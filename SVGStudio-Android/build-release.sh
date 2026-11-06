#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
cd "$(dirname "$0")"
echo "=== SVG Studio 1.1 release build ==="
command -v java >/dev/null || { echo "Java missing: pkg install openjdk-17"; exit 1; }
command -v gradle >/dev/null || { echo "Gradle missing: pkg install gradle"; exit 1; }
: "${ANDROID_HOME:=$HOME/android-sdk}"
: "${ANDROID_SDK_ROOT:=$ANDROID_HOME}"
export ANDROID_HOME ANDROID_SDK_ROOT
[ -d "$ANDROID_HOME/platforms/android-35" ] || {
  echo "Missing Android SDK Platform 35 at: $ANDROID_HOME/platforms/android-35"
  echo "Install Android SDK Platform 35 and Build Tools 35.0.0, then retry."
  exit 1
}
gradle --no-daemon --stacktrace clean :app:assembleRelease
APK="app/build/outputs/apk/release/app-release.apk"
[ -s "$APK" ] || { echo "Build failed: release APK not found."; exit 1; }
mkdir -p dist
cp "$APK" "dist/SVG-Studio-1.1-release.apk"
echo
echo "Build successful:"
echo "$(pwd)/dist/SVG-Studio-1.1-release.apk"
