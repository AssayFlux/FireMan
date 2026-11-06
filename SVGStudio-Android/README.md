# SVG Studio for Android

An offline Android SVG creator/editor with a live preview, editable XML source, clipboard copy, and SVG export.

## Features
- Prompt-guided offline starter designs (logo, mountain, sun, heart, flowchart, geometric artwork).
- Live SVG preview.
- Editable SVG/XML source.
- Copy source code to clipboard.
- Export `.svg` through Android's document picker.
- No account, network permission, API key, or AI service required.

> The prompt generator is a lightweight local template generator, not a cloud generative-AI model. You can edit the generated SVG source directly for complete control.

## Build in Termux

Recommended: Android device with Termux, Java 17, Gradle, and Android SDK platform 35 installed and configured.

```bash
pkg update
pkg install openjdk-17 gradle git unzip
unzip SVGStudio-Android.zip
cd SVGStudio-Android
export ANDROID_HOME="$HOME/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
chmod +x build-release.sh
./build-release.sh
```

If your SDK is stored elsewhere, set `ANDROID_HOME` to that path. Install Android SDK Platform 35 and Build Tools 35.0.0 using your SDK manager before building.

The output is copied to `dist/SVG-Studio-release-unsigned-or-signed.apk`.

## Create a signed release APK

For a distributable release, create a keystore and keep it private:

```bash
keytool -genkeypair -v -keystore svgstudio-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias svgstudio
```

Then set the environment variables before running the build:

```bash
export SIGNING_STORE_FILE="$PWD/svgstudio-release.jks"
export SIGNING_STORE_PASSWORD='your-keystore-password'
export SIGNING_KEY_ALIAS='svgstudio'
export SIGNING_KEY_PASSWORD='your-key-password'
./build-release.sh
```

Never commit your keystore or passwords to GitHub. Keep a secure backup; losing the keystore can prevent future updates from being signed with the same identity.

## Project
- Namespace/application ID: `org.assayflux.svgstudio`
- Language: Kotlin
- UI: Android Views
- Minimum Android: 6.0 (API 23)
- Target/compile SDK: 35
- Java: 17
- Version: 1.0.0

## Notes
This is a starter release project. It generates useful editable SVG templates locally; it does not claim to convert arbitrary natural-language descriptions into fully original AI artwork. Validate the project against your exact Termux SDK installation before publishing.
