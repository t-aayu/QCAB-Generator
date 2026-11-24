# Publishing QCAB Generator to GitHub

This guide walks through publishing this Android project to your GitHub account (**[@t-aayu](https://github.com/t-aayu)**) and cutting your first release.

---

## 1. Create the Repository on GitHub

1. Sign in to GitHub as **t-aayu** and navigate to [github.com/new](https://github.com/new).
2. Set the **Repository name** to `QCAB-Generator`.
3. Add a short description:
   > Native Android app to generate standard UPSC CSE Question cum Answer Booklets (QCAB) as printable A4 PDFs.
4. Choose **Public** (recommended for open source) or **Private**.
5. Leave **"Add a README file"**, **".gitignore"**, and **"Choose a license"** unchecked (these files are already included).
6. Click **Create repository**.

---

## 2. Verify Sensitive Files Are Ignored

Before committing, confirm that no private credentials or keystores are staged:

- `.env` (ignored in `.gitignore`)
- `local.properties` (ignored in `.gitignore`)
- `debug.keystore`, `*.jks`, `*.keystore` (ignored in `.gitignore`)
- `build/` outputs, `*.apk`, `*.aab` (ignored in `.gitignore`)

---

## 3. Initialize Git and Push to GitHub

Run the following commands from the project root directory:

```bash
git init
git branch -M main
git add .
git status   # Double-check no .env or .jks/.keystore files appear
git commit -m "Initial release: UPSC CSE QCAB Generator Android app"
git remote add origin https://github.com/t-aayu/QCAB-Generator.git
git push -u origin main
```

---

## 4. GitHub Secrets & CI Configuration

- **Debug CI (`.github/workflows/android-ci.yml`):** **No GitHub Secrets are required.** The workflow generates a temporary `debug.keystore` automatically, and this app runs 100% offline (no `GEMINI_API_KEY` or Firebase secrets are needed).
- **Automated APK Release (`.github/workflows/release.yml`):** Add these **3 repository secrets** under **Settings → Secrets and variables → Actions** before tagging a release:
  1. `RELEASE_KEYSTORE_BASE64` — base64-encoded `.jks` upload keystore (with key alias `upload`)
  2. `STORE_PASSWORD` — keystore password
  3. `KEY_PASSWORD` — `upload` key password

Once pushed, open **Actions** in your GitHub repository (`https://github.com/t-aayu/QCAB-Generator/actions`).
The **Android CI** workflow (`.github/workflows/android-ci.yml`) will automatically:
1. Generate a CI debug keystore.
2. Build the debug APK (`assembleDebug`).
3. Run unit tests (`testDebugUnitTest`).
4. Upload `app-debug.apk` as a downloadable build artifact.

---

## 5. Create a Release with a Signed APK

### Generate an Upload Keystore (One-Time Setup)

Keep this file strictly outside your Git repository and backed up securely (the key alias must be `upload` to match `app/build.gradle.kts`):

```bash
keytool -genkeypair -v \
  -keystore ../my-upload-key.jks \
  -alias upload \
  -keyalg RSA -keysize 2048 -validity 10000
```

Encode it to base64 to paste into the `RELEASE_KEYSTORE_BASE64` GitHub Secret:

```bash
# Linux
base64 -w 0 ../my-upload-key.jks

# macOS
base64 -i ../my-upload-key.jks | tr -d '\n'
```

### Option A: Automated Release via GitHub Actions (Recommended)

Push a version tag (e.g., `v1.0.0`) and `.github/workflows/release.yml` will automatically run tests, build the signed `.apk` and `.aab`, generate `SHA256SUMS.txt`, and publish a GitHub Release:

```bash
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

You can also trigger a release manually from **Actions → Release APK → Run workflow**.

### Option B: Build Signed Release Locally

```bash
export KEYSTORE_PATH="../my-upload-key.jks"
export STORE_PASSWORD="YOUR_STORE_PASSWORD"
export KEY_PASSWORD="YOUR_KEY_PASSWORD"

gradle :app:assembleRelease
```

The signed APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

---

## 6. Recommended Repository Topics

Add these topics in your GitHub repository **About** sidebar to improve discoverability:

`android` · `kotlin` · `jetpack-compose` · `material3` · `upsc` · `upsc-cse` · `qcab` · `pdf-generator`
