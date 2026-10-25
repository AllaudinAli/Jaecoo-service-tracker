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
if ($Fresh) { & $adb shell pm clear $app | Out-Null }
# -S stops any running copy first so the new build always starts cleanly.
cmd /c "`"$adb`" shell am start -S -n $app/.MainActivity 2>&1" | Out-Null
Write-Host "Running in the emulator window."
