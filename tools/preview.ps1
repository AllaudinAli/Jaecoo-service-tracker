# Opens the app in the Android emulator on this PC.
#   .\tools\preview.ps1           build, install and launch (keeps app data)
#   .\tools\preview.ps1 -Fresh    also wipe app data to see first-launch setup
param([switch]$Fresh)

# adb writes harmless warnings to stderr, which PowerShell 5.1 would treat as fatal under "Stop".
$ErrorActionPreference = "Continue"
$root = Split-Path $PSScriptRoot -Parent
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
$adb = "$sdk\platform-tools\adb.exe"
$app = "com.example.j5servicebook"
if (-not $env:JAVA_HOME) { $env:JAVA_HOME = "$env:LOCALAPPDATA\Android\jdk-21" }

# Start the emulator window unless one is already running.
if (-not ((& $adb devices) -match "emulator-\d+\s+device")) {
    Write-Host "Starting emulator..."
    Start-Process "$sdk\emulator\emulator.exe" -ArgumentList "-avd j5test -no-audio -no-boot-anim"
    & $adb wait-for-device
    while ((& $adb shell getprop sys.boot_completed 2>$null) -ne "1") { Start-Sleep 2 }
}

Write-Host "Building..."
Push-Location $root
try { & .\gradlew.bat :app:assembleDebug -q } finally { Pop-Location }
if ($LASTEXITCODE -ne 0) { Write-Host "Build failed."; exit 1 }
$apk = "$root\app\build\outputs\apk\debug\app-debug.apk"

# A build signed with a different key can't update in place, so reinstall.
$out = cmd /c "`"$adb`" install -r `"$apk`" 2>&1" | Out-String
if ($out -match "INSTALL_FAILED_UPDATE_INCOMPATIBLE|signatures do not match") {
    & $adb uninstall $app | Out-Null
    & $adb install $apk | Out-Null
}
# Stop, optionally wipe, then start in a clean task. System UI can relaunch the app right after a
# force-stop, and a data wipe then kills that launch and leaves a dead, blank task behind, so wait
# for the wipe to settle and retry if the process didn't come up.
& $adb shell am force-stop $app | Out-Null
if ($Fresh) { & $adb shell pm clear $app | Out-Null; Start-Sleep 2 }
foreach ($attempt in 1..3) {
    cmd /c "`"$adb`" shell am start -S -W -f 0x10008000 -n $app/.MainActivity 2>&1" | Out-Null
    Start-Sleep 2
    if (& $adb shell pidof $app) { break }
}
Write-Host "Running in the emulator window."
