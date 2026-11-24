# UPSC CSE QCAB Generator (Android)

[![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Android CI](https://github.com/t-aayu/qcab-generator/actions/workflows/android-ci.yml/badge.svg)](https://github.com/t-aayu/qcab-generator/actions/workflows/android-ci.yml)

A native Android application built with **Kotlin** and **Jetpack Compose** for generating standard **UPSC CSE Question cum Answer Booklets (QCAB)** as printable A4 PDFs—completely offline and with zero storage permissions required.

---

## Features

- **Authentic UPSC A4 QCAB Layout (`210 × 297 mm`):**
  - Standard `25 mm` left and right vertical margin rules on every page.
  - Bundled **PT Serif** typography for crisp printed questions, numbers (`Q. 1`, `Q. 2`, …), and right-aligned marks stamps (`[10 Marks]`).
  - Official right-margin notice (*"Candidates must not write on this margin"*).
  - Optional italic bottom-left **Test Name / Footer Stamp** on every page.
- **Flexible Question Authoring (1–20 Questions):**
  - Quick presets (`1`, `5`, `10`, `20 Full`) and smart state preservation when navigating between setup and questions.
  - Mutually exclusive **Question Text** or **Image Attachment** (ideal for equations, maps, and diagrams) via Android's zero-permission **Photo Picker**.
  - One-tap **Clear Text / Clear Image** toggles with instant **Undo** support.
  - Customizable **Marks Stamp** and **Pages Required** per question.
  - Confirmation prompt before generating booklets with empty questions.
- **Built-in A4 PDF Preview & Export:**
  - Instant scrollable page-by-page preview powered by Android's native `PdfRenderer`.
  - **Save to Downloads** (via `MediaStore` on Android 10+ / Storage Access Framework on older versions), **Save As** (custom folder or cloud drive), and **Share PDF** via system share sheet.
- **System / Light / Dark Theme:**
  - Top-level theme mode switcher defaulting to **System**, persisted locally via Jetpack DataStore Preferences.

---

## Tech Stack

| Layer | Technology |
| :--- | :--- |
| Language | Kotlin |
| UI Framework | Jetpack Compose & Material Design 3 |
| Architecture | MVVM (`AndroidViewModel` + `StateFlow`) |
| PDF Generation & Viewing | Native Android `PdfDocument` & `PdfRenderer` |
| Image Loading & Picking | Coil Compose & Android Photo Picker (`PickVisualMedia`) |
| Preferences | Jetpack DataStore Preferences |
| Typography | Bundled `PT Serif` & `Inter` |

---

## Project Structure

```text
app/src/main/java/com/example/
├── MainActivity.kt                  # Edge-to-edge host, theme resolution, step transitions
├── data/
│   └── ThemePreferences.kt          # DataStore Preferences for System/Light/Dark mode
├── model/
│   └── QcabModels.kt                # Immutable UI state, QuestionItem, ThemeMode
├── pdf/
│   └── QcabPdfGenerator.kt          # Native A4 PdfDocument engine & export helpers
├── viewmodel/
│   └── QcabViewModel.kt             # Unidirectional state & async PDF build workflow
└── ui/
    ├── components/
    │   ├── QcabHeader.kt            # Hero header, theme toggles & live mini A4 preview
    │   └── QuestionCard.kt          # Question editor card (text/image, marks, pages)
    ├── screens/
    │   ├── SetupScreen.kt           # Step 1: Test Configuration
    │   ├── QuestionsScreen.kt       # Step 2: Question Details Editor
    │   └── PdfPreviewScreen.kt      # Step 3: Native A4 PDF Preview & Export
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

## Getting Started

### Prerequisites

- **Android Studio** (Meerkat / Ladybug or newer) with **Android SDK 36** installed
- **JDK 17** or newer
- Android device or emulator running **Android 7.0 (API 24)** or higher

### Clone & Run Locally

```bash
git clone https://github.com/t-aayu/qcab-generator.git
cd qcab-generator
```

1. Open the project folder in **Android Studio**.
2. Wait for Gradle sync to complete.
3. Select a connected Android device or emulator and click **Run**.

### Build via Command Line

Ensure you have a `debug.keystore` in the project root (or generate a standard Android debug keystore once):

```bash
keytool -genkeypair -v \
  -keystore debug.keystore \
  -storepass android \
  -alias androiddebugkey \
  -keypass android \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Android Debug,O=Android,C=US"
```

Then build and test:

```bash
# Build Debug APK
gradle :app:assembleDebug

# Run Unit & Robolectric Tests
gradle :app:testDebugUnitTest
```

### Release Builds

To build a signed release APK or Android App Bundle (`.aab`), set your signing environment variables:

```bash
export KEYSTORE_PATH="/path/to/my-upload-key.jks"
export STORE_PASSWORD="your_keystore_password"
export KEY_PASSWORD="your_key_password"

gradle :app:assembleRelease
# or for Google Play App Bundle:
gradle :app:bundleRelease
```

---

## Publishing to GitHub

See [PUBLISHING.md](PUBLISHING.md) for a step-by-step guide to pushing this repository to `https://github.com/t-aayu/qcab-generator`, enabling GitHub Actions CI, and publishing versioned APK releases.

---

## Contributing

Contributions, bug reports, and feature suggestions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

---

## Author

- **[@t-aayu](https://github.com/t-aayu)**

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.
