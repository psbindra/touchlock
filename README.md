# TouchLock for Video Calls — Android Native App

TouchLock prevents accidental touches, accidental hang-ups, and muting when handing your Android phone to babies, toddlers, or kids during video calls (WhatsApp, Zoom, FaceTime, Google Meet, Teams).

## 🚀 How to Build APK (3 Simple Steps)

### Option A: Using Android Studio (Recommended)
1. Extract this ZIP file.
2. Open **Android Studio** -> click **Open** -> select this project directory.
3. Wait for Gradle Sync to complete.
4. Go to **Build** menu -> **Build Bundle(s) / APK(s)** -> **Build APK(s)**.
5. Android Studio will generate the installable **`app-debug.apk`** in:
   `app/build/outputs/apk/debug/app-debug.apk`
6. Transfer `app-debug.apk` to your Android phone and install!

### Option B: Using Command Line / Terminal
```bash
# On Linux/macOS:
./gradlew assembleDebug

# On Windows:
gradlew.bat assembleDebug
```
The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

### Option C: GitHub Actions (Free Cloud Build)
This project includes `.github/workflows/build-apk.yml`. If you push this repository to GitHub, GitHub Actions will automatically compile the APK and make it available for 1-click download under the Actions tab / Releases!

---

## 📱 Features Included
- **Quick Settings Tile**: Pull down Notification Shade and tap the "Touch Lock" tile anytime during a video call.
- **Overlay Window Touch Absorption**: Blocks all accidental touches, home/back navigation, and status bar taps while keeping the video call stream crystal clear.
- **Parent Unlock Pattern**: Unlock anytime by connecting your secret 3x3 pattern.
- **Emergency PIN Fallback**: Backup 4-digit PIN in case pattern is forgotten.
- **Kid-Proof Visual Bubbles**: Interactive floating feedback bubbles to entertain toddlers without disrupting the call.
