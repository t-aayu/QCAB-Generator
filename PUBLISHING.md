# Publishing QCAB Generator to GitHub

This guide walks through publishing this Android project to your GitHub account (**[@t-aayu](https://github.com/t-aayu)**) and cutting your first release.

---

## 1. Create the Repository on GitHub

1. Sign in to GitHub as **t-aayu** and navigate to [github.com/new](https://github.com/new).
2. Set the **Repository name** to `qcab-generator` (or update the badge/clone URLs in `README.md` if you choose a different name).
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
git remote add origin https://github.com/t-aayu/qcab-generator.git
git push -u origin main
```

---

## 4. Verify GitHub Actions CI

Once pushed, open **Actions** in your GitHub repository (`https://github.com/t-aayu/qcab-generator/actions`).
The **Android CI** workflow (`.github/workflows/android-ci.yml`) will automatically:
1. Generate a CI debug keystore.
2. Build the debug APK (`assembleDebug`).
3. Run unit tests (`testDebugUnitTest`).
4. Upload `app-debug.apk` as a downloadable build artifact.

---

## 5. Create a Release with a Signed APK (Optional)

### Generate an Upload Keystore (One-Time Setup)

Keep this file strictly outside your Git repository and backed up securely:

```bash
keytool -genkeypair -v \
  -keystore ../my-upload-key.jks \
  -alias upload \
  -keyalg RSA -keysize 2048 -validity 10000
```

### Build the Signed Release APK

```bash
export KEYSTORE_PATH="../my-upload-key.jks"
export STORE_PASSWORD="YOUR_STORE_PASSWORD"
export KEY_PASSWORD="YOUR_KEY_PASSWORD"

gradle :app:assembleRelease
```

The signed APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

### Tag and Publish on GitHub Releases

```bash
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

Then go to **Releases → Draft a new release** on GitHub:
1. Select tag `v1.0.0`.
2. Title it `QCAB Generator v1.0.0`.
3. Paste the release notes from `CHANGELOG.md`.
4. Drag and drop `app-release.apk` (renamed e.g. `qcab-generator-v1.0.0.apk`) into the release binaries box.
5. Click **Publish release**.

---

## 6. Recommended Repository Topics

Add these topics in your GitHub repository **About** sidebar to improve discoverability:

`android` · `kotlin` · `jetpack-compose` · `material3` · `upsc` · `upsc-cse` · `qcab` · `pdf-generator`
