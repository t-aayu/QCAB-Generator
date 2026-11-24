# Contributing to QCAB Generator

Thank you for your interest in contributing to **QCAB Generator** (`t-aayu/qcab-generator`)!

## Ways to Contribute

- **Report bugs** using the [Bug Report](.github/ISSUE_TEMPLATE/bug_report.md) template.
- **Suggest enhancements** using the [Feature Request](.github/ISSUE_TEMPLATE/feature_request.md) template.
- **Submit pull requests** for bug fixes, PDF layout improvements, accessibility enhancements, or new features.

## Development Workflow

1. Fork the repository at `https://github.com/t-aayu/qcab-generator`.
2. Create a descriptive feature or fix branch from `main`:
   ```bash
   git checkout -b feat/your-feature-name
   ```
3. Make your changes following the project guidelines:
   - Keep UI written in **Jetpack Compose** with **Material Design 3** tokens from `MaterialTheme.colorScheme` / `LocalQcabColors` (avoid hardcoded hex colors in composables).
   - Maintain **zero broad storage permissions** (use Photo Picker, MediaStore, SAF, and FileProvider).
   - Ensure interactive elements meet the **48dp minimum touch target** and include meaningful `contentDescription` and `testTag` modifiers.
   - Put user-facing copy in `app/src/main/res/values/strings.xml`.
4. Verify your build and unit tests pass locally:
   ```bash
   gradle :app:assembleDebug
   gradle :app:testDebugUnitTest
   ```
5. Push your branch and open a Pull Request against `main` using the PR template.

## Commit Style

Use clear, concise commit messages, for example:
- `feat: add landscape booklet layout option`
- `fix: align right-margin warning baseline on narrow devices`
- `docs: update release signing instructions`
