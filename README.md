# FireMan — SVG Studio Android

The complete SVG Studio 1.1 Android source project is published as a ZIP archive in this repository.

## Download
- [Download the complete project ZIP](./SVGStudio-Android-Pro.zip)

## What's inside
- Native Kotlin Android application.
- SVG vector tools: rectangle, circle, ellipse, line, text, star, triangle, and custom paths.
- Linear gradients, fill/stroke styling, and stroke width.
- Layer visibility, opacity, selection, editing, reordering, and deletion.
- Canvas sizing, live preview, editable SVG/XML source, clipboard copy.
- SVG import and export using Android's document picker.
- Termux release build script and project instructions.

## Build
Extract the ZIP in Termux, install JDK 17, Gradle 8.9+, Android SDK Platform 35 and Build Tools 35.0.0, then run:

```bash
cd SVGStudio-Android
chmod +x build-release.sh
./build-release.sh
```

The APK is produced at `dist/SVG-Studio-1.1-release.apk`. Without signing environment variables the output is unsigned. See the included project README for keystore signing instructions.

## Project identity
- Application ID: `org.assayflux.svgstudio`
- Kotlin / Android Views
- Minimum SDK: API 23
- Version: 1.1.0

**Build status:** Source published; no APK build or device validation is claimed by this repository update.
