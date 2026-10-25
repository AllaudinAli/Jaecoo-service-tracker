# J5 Service Book

An offline Android app that tracks maintenance and warranty for a Jaecoo J5 SHS hybrid (Pakistan market). You enter dates and odometer readings. The app works out what is due and when, and reminds you.

- Fully offline: no internet permission, no account, no backend. Data is stored on the phone (Room/SQLite).
- Schedule and warranty rules come from the owner's manual (pp. 226–234) and warranty booklet (pp. 4–6), in [`ScheduleRules.kt`](app/src/main/java/com/example/j5servicebook/domain/ScheduleRules.kt).
- JSON export and import (Settings → Backup) is the backup.

The full spec is in [`J5 Service Book — Android App Plan.md`](J5%20Service%20Book%20—%20Android%20App%20Plan.md).

## Install on the phone

1. Copy `dist/J5ServiceBook-<version>.apk` to the phone (USB cable, Google Drive, or Quick Share).
2. Open it from **My Files → Downloads** (or wherever you saved it).
3. If Android asks, allow **Install unknown apps** for that app (My Files or Drive), then tap **Install**.
4. If Play Protect warns that the app is unknown, tap **More details → Install anyway**. That warning appears for any app that isn't from the Play Store.

To update, install a newer APK over the old one. Your data is kept. This only works if the new APK is signed with the same key (see below).

## Build

Needs JDK 17+ and the Android SDK (Android Studio includes both).

```sh
./gradlew test             # schedule engine unit tests
./gradlew assembleDebug    # app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease  # signed APK, needs keystore.properties
```

### Release signing key

Release builds are signed with a key that is **not** in this repo. `keystore.properties` in the project root points at it:

```properties
storeFile=C:/Users/<you>/.j5servicebook/release.jks
storePassword=...
keyAlias=j5
keyPassword=...
```

**Back up `release.jks` and `keystore.properties` somewhere safe** (a password manager or a private cloud folder). If you lose them you can still build the app, but the phone won't accept it as an update. You would have to uninstall, which deletes the app's data unless you export a backup first.

## Preview on a PC

`.\tools\preview.ps1` builds the app and opens it in the Android emulator (`-Fresh` wipes data to show first-launch setup).

## Credits

- Car photo: "2025 Jaecoo 5 EV Max (Thailand)" by iMoD Official, [CC BY 3.0](https://creativecommons.org/licenses/by/3.0/), via Wikimedia Commons. Background removed and number plate covered. Shown in the app under Settings → About.
- Font: [Geist](https://github.com/vercel/geist-font), SIL Open Font License 1.1 (see `licenses/Geist-OFL.txt`).

## Code layout

| Path | What |
| --- | --- |
| `domain/` | Pure Kotlin, no Android imports: rules, `ScheduleEngine`, `ReminderPlanner`, backup format |
| `data/` | Room database, repository, DataStore settings |
| `reminders/` | Daily WorkManager check and notifications |
| `ui/` | Compose screens, sheets, theme |
