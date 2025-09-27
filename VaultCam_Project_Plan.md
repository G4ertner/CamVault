# VaultCam — Project Plan

**Owner:** Nikolas  
**Roles:** You (PM/QA), Codex CLI (dev), ChatGPT (PM/architect)  
**Elevator Pitch:** A secure camera + vault. Capture without auth; viewing/exporting requires biometrics. All media is encrypted and kept out of the public gallery unless explicitly exported.

---

## 1) Product Brief

### Vision
VaultCam lets users take photos (and later, videos) that are **never** written to the public gallery. Media is **encrypted at rest** in app-private storage. Users can view/manage items only after biometric unlock. Export to the system gallery is explicit and gated by an extra biometric step.

### Key Behaviors
- **Start Screen:** two big actions — **Secure Camera** and **Photo Vault**.
- **Camera Path (no auth):** take a photo → app **encrypts** and stores it → show “Saved to Vault” snackbar; remain in camera.
- **Vault Path (auth required):** biometric prompt → on success show grid of thumbnails (decrypted in memory). Detail viewer supports **lossless rotate** (EXIF flip), delete, and **export to MediaStore**.
- **Orientation:** camera capture and viewer respect device rotation; photos store correct EXIF; viewer autorotates; manual rotate available in vault.

### Security Model (v1)
- **Crypto:** Tink AEAD (AES‑GCM or AES‑GCM‑SIV) data key; **wrapped** by Android Keystore key. Keyset lives only in app-private storage.
- **No plaintext on disk:** including thumbnails; decrypt only to memory.
- **Screenshots disabled** on vault and viewer routes (and recents snapshot disabled where supported).
- **Export is explicit** (warning + secondary biometric) and creates a public copy in MediaStore; original remains encrypted in vault.

### Non-Goals (v1)
- Cloud sync/backup, albums/tags, advanced editing, direct sharing to other apps (beyond export to Photos).

---

## 2) Core Flows

**Flow A — Capture**  
Start → Camera → Shutter → Encrypt & Save → Snackbar “Saved to Vault.” (Stay on camera.)

**Flow B — View**  
Start → Vault → Biometric → Grid → Tap → Detail Viewer → (Rotate / Delete / Export).

**Flow C — Export**  
Viewer → Export → Warning + Biometric → Decrypt (memory) → Write to MediaStore → Toast “Exported.”

---

## 3) Functional Requirements

- **Camera (v1):** photo capture (JPEG/HEIC), tap-to-focus, pinch-to-zoom, flash toggle, front/back switch, grid overlay.
- **Orientation:** `ImageCapture.targetRotation` set from display; EXIF orientation written; viewer autorotates; **lossless rotate** updates EXIF and re-encrypts.
- **Vault:** grid (lazy) with RAM-only thumbnail cache; detail viewer (zoom/pan), rotate, delete, export.
- **Auth:** biometric required for Vault and any action that reveals plaintext (export). Camera is open.
- **Storage:** app-private `noBackupFilesDir/secure_media` with filenames `{uuid}.enc`.
- **Crypto:** Tink AEAD; associatedData = UUID bytes; keyset wrapped by Keystore and stored at `/files/keys/keyset.json` (app-private).
- **Session timeout:** re-auth after N minutes or when app backgrounds.

---

## 4) Architecture

- **UI:** Jetpack Compose, single-activity, `NavHost` routes: `start`, `camera`, `vault`, `viewer/{id}`, `settings`.
- **Camera:** CameraX (`Preview`, `ImageCapture`).
- **Crypto:** Tink keyset wrapped by Android Keystore.
- **Storage:** `VaultRepository` (save, list, decrypt, rotate, delete, export).
- **State:** `VaultSession` (unlocked flag + timer).
- **Tests:** JVM (crypto, repo), Instrumented (navigation, biometric gate, capture via fakes), Compose UI tests.

**Suggested package map**
```
dev.nik.vaultcam/
  app/ (Application)
  ui/
    start/StartScreen.kt
    camera/CameraScreen.kt
    vault/VaultScreen.kt
    viewer/ViewerScreen.kt
    settings/SettingsScreen.kt
    nav/NavGraph.kt
  data/
    repo/VaultRepository.kt
    model/VaultItem.kt
  crypto/
    TinkModule.kt
    KeyStoreWrapper.kt
  session/
    VaultSession.kt
  util/
    Orientation.kt
    SecureScreens.kt
```

---

## 5) Build Plan for Codex CLI (Phased)

> Each step lists **Goal → Codex tasks → Automated tests (Codex) → Manual verify (You)**.  
> Feed to Codex one step at a time; run automated tests; then do the manual checks.

### Phase 0 — Scaffold & Dependencies

#### 0.1 Project Skeleton
**Goal:** New Compose app, minSdk 26+, Kotlin DSL.  
**Codex tasks:**
- Create project “VaultCam” (package `dev.nik.vaultcam`) with Compose (Material 3) and a `StartScreen` with two buttons.
- Add `NavHost` with routes `start`, `camera`, `vault`, `viewer/{id}` (empty for now).  
**Automated:** CI build (`./gradlew assembleDebug`) + a trivial Compose test asserting “StartScreen” renders.  
**Manual:** Launch app → see Start screen.

#### 0.2 Dependencies
**Goal:** Add libraries via `libs.versions.toml`.  
**Codex tasks:** Add CameraX (core, camera2, lifecycle, view, video), Biometric, Tink, Coil-compose, coreLibraryDesugaring.  
**Automated:** Build passes.

#### 0.3 Secure UI Defaults
**Goal:** Block screenshots on sensitive routes.  
**Codex tasks:** Implement `SecureScreens` to toggle `FLAG_SECURE` and, on Android 14+, `android:recentsScreenshotEnabled="false"` for Vault/Viewer only.  
**Automated:** Unit test for helper logic (sensitive routes return true).  
**Manual:** (Later) Verify on Vault/Viewer screenshots are blocked.

---

### Phase 1 — Navigation & Auth Shell

#### 1.1 Navigation Graph
**Goal:** Working nav from Start to Camera/Vault.  
**Codex tasks:** Implement `NavHost` + Start buttons navigation.  
**Automated:** Compose UI test clicking “Secure Camera” shows `camera` route.  
**Manual:** Buttons navigate as expected.

#### 1.2 Biometric Gate (Vault Only)
**Goal:** Camera is open; Vault requires biometric.  
**Codex tasks:** Add `VaultSession` (unlocked flag + timer); `BiometricGate` wrapper that prompts on `vault` enter; on success, set unlocked for 5 minutes; on cancel, back to Start.  
**Automated:** Instrumented test injects fake biometric → verifies grid scaffold appears only on success.  
**Manual:** Enter Vault → biometric prompt; cancel → returns to Start.

---

### Phase 2 — Camera Capture (No Plaintext on Disk)

#### 2.1 Camera Preview + Controls
**Goal:** Live preview; tap-to-focus; pinch-to-zoom; flash toggle; front/back switch; grid overlay.  
**Codex tasks:** CameraX `PreviewView` via `AndroidView`; bind lifecycle; implement gestures; toggles.  
**Automated:** UI test ensures controls exist; state changes reflected in controller.  
**Manual:** Preview responds; flip camera; zoom/focus work.

#### 2.2 Correct Orientation on Capture
**Goal:** Proper EXIF/rotation handling.  
**Codex tasks:** Set `ImageCapture.targetRotation(display.rotation)`; ensure EXIF orientation written.  
**Automated:** Unit test for helper that maps display rotation → EXIF tag.  
**Manual:** Shoot in portrait/landscape; later viewer shows correct orientation.

#### 2.3 Crypto Bootstrap
**Goal:** Tink ready; keyset wrapped by Keystore.  
**Codex tasks:** Application: `AeadConfig.register()`; create `KeyStoreWrapper` and `TinkModule` to store encrypted keyset at `/files/keys/keyset.json`.  
**Automated:** JVM tests for keyset create/load and AEAD roundtrip.  
**Manual:** First run produces `/files/keys/...` (Device File Explorer).

#### 2.4 Encrypt on Capture
**Goal:** Never write plaintext; save `{uuid}.enc`.  
**Codex tasks:** `VaultRepository.saveEncrypted(bytes, uuid, ad=uuid)`; integrate shutter; snackbar “Saved to Vault.”  
**Automated:** Repo test: ciphertext exists, decrypt = original; ensure no temp plaintext files.  
**Manual:** Capture → snackbar shows; `.enc` appears; file looks like gibberish in hex.

---

### Phase 3 — Vault List & Viewer

#### 3.1 Vault Grid (Unlocked)
**Goal:** Thumbnails from decrypted bytes; RAM-only cache.  
**Codex tasks:** `listItems()` for metadata; `thumbnailLoader` decrypts & decodes to `ImageBitmap`; LRU memory cache.  
**Automated:** Unit test: decrypt path doesn’t write temp files; UI test: grid shows 1 fake item.  
**Manual:** After a capture, grid shows thumb; scroll smooth.

#### 3.2 Detail Viewer
**Goal:** Zoom/pan; delete.  
**Codex tasks:** `viewer/{id}` decrypts to memory and displays image; delete removes file and pops.  
**Automated:** Repo test for delete; UI test: open → delete → list count decrements.  
**Manual:** Open photo; delete works; item gone on back.

#### 3.3 Lossless Rotate
**Goal:** Rotate via EXIF without re-encode (fallback re-encode if needed).  
**Codex tasks:** Read bytes → update EXIF orientation → re-encrypt; add rotate left/right actions.  
**Automated:** Byte-level test: rotate twice restores original orientation; metadata updated.  
**Manual:** Rotate; reopen viewer; orientation persists.

---

### Phase 4 — Export to Public Gallery (Explicit & Safe)

#### 4.1 Export Flow
**Goal:** Copy decrypted image into MediaStore.  
**Codex tasks:** “Export to Photos” button → warning dialog → **secondary biometric** → decrypt to memory → insert into `MediaStore.Images` with proper EXIF/rotation → toast “Exported.”  
**Automated:** Instrumented test with fake repo asserts MediaStore insert called.  
**Manual:** Export one item; verify it appears in Photos; original stays in vault.

#### 4.2 Audit Log & Settings
**Goal:** Simple settings + local audit.  
**Codex tasks:** `DataStore` for unlock timeout and haptics; append `audit.log` (app-private) on export/delete/rotate.  
**Automated:** Tests verify settings persistence and log writing.  
**Manual:** Change timeout; confirm relock; check log file size grows.

---

### Phase 5 — Reliability & Polish

#### 5.1 Session Timeout & Lifecycle
**Goal:** Auto-lock on timeout/background.  
**Codex tasks:** Observe foreground/background; clear `VaultSession` and cancel decrypt jobs on pause.  
**Automated:** Timer unit test; instrumented background/return re-prompts.  
**Manual:** Background 5+ mins → vault demands auth again.

#### 5.2 Errors & Low-Storage
**Goal:** Clean failure modes; no leaks.  
**Codex tasks:** try/catch around capture/encrypt/export; user-friendly errors; zero out sensitive byte arrays after use.  
**Automated:** Simulated IO error leaves no plaintext; error propagated.  
**Manual:** Fill emulator storage; capture/export produce safe errors.

#### 5.3 Branding & Accessibility
**Goal:** Production-ready feel.  
**Codex tasks:** Adaptive icon; dark theme; content descriptions; large tap targets; optional haptics.  
**Automated:** Lint and accessibility checks.  
**Manual:** Quick UX sweep.

---

## 6) Stretch: Video (Optional)

#### S.1 Encrypted Video
**Goal:** Record MP4 and encrypt.  
**Codex tasks:** CameraX `Recorder` → stream or chunk-encrypt to `{uuid}.venc`; generate thumbnail from keyframe in memory; viewer plays decrypted stream.  
**Automated:** Chunked encrypt/decrypt tests; UI shows video badge.  
**Manual:** 5s clip; playback inside viewer.

---

## 7) Exact Codex Prompts (First 6 Steps)

**Step 0.1 — Project Skeleton**  
Create a new Android app named “VaultCam” (package `dev.nik.vaultcam`) using Kotlin + Jetpack Compose (Material 3), minSdk 26, Gradle Kotlin DSL. Add a `StartScreen` with two buttons: “Secure Camera” and “Photo Vault”. Wire a `NavHost` with routes `start`, `camera`, `vault`, `viewer/{id}` (empty screens for now). Ensure the app builds and runs.

**Step 0.2 — Dependencies**  
Add dependencies in `libs.versions.toml` and module `build.gradle.kts`: CameraX (core, camera2, lifecycle, view, video), Biometric, Tink, Coil-compose, coreLibraryDesugaring. Sync and ensure `./gradlew assembleDebug` succeeds.

**Step 0.3 — Secure UI Defaults**  
Implement a `SecureScreens` helper to toggle `FLAG_SECURE` and set `android:recentsScreenshotEnabled="false"` for vault and viewer routes only. Leave camera and start unsecured. Add unit tests for the helper.

**Step 1.1 — Navigation Graph**  
Implement `StartScreen` to navigate to `camera` and `vault`. Add a Compose UI test that clicks “Secure Camera” and asserts the `camera` route is shown.

**Step 1.2 — Biometric Gate (Vault Only)**  
Add a `VaultSession` with unlocked flag + timer. Implement a `BiometricGate` that shows `BiometricPrompt` upon entering `vault`. On success, set session unlocked for 5 minutes; on cancel, navigate back to `start`. Add an instrumented test that injects a fake biometric callback and verifies the unlocked vault scaffold appears only after success.

**Step 2.1 — Camera Preview + Controls**  
Implement `CameraScreen` with CameraX `PreviewView` (AndroidView), bind lifecycle, add tap-to-focus, pinch-to-zoom, flash toggle, and front/back camera switch. Provide simple grid overlay. Add tests that verify control states update accordingly.

*(When ready, request the next batch for Steps 2.2–3.1.)*

---

## 8) Acceptance Checklist (v1)

- Start screen with two options; Camera works without auth; Vault gated by biometric.  
- Orientation correct in viewer; manual rotate persists (lossless where possible).  
- Media **never** visible in system gallery unless exported.  
- All media stored as encrypted `.enc` under app-private storage; **no plaintext temp files**.  
- Session re-auth after timeout/background.  
- Unit + instrumented tests pass.

---

## 9) Risks & Mitigations (brief)

- **EXIF-only rotation limits:** Some inputs may lack EXIF; fallback to re-encode (costly).  
- **Low-storage writes:** Preflight free space check; stream or chunk where possible.  
- **Keystore quirks across OEMs:** Keep wrapper minimal; add telemetry to audit log for failures.  
- **CameraX device differences:** Feature-flag advanced controls per capability; keep defaults simple.

---

## 10) Out-of-Scope (now)

- Cloud sync, multi-device accounts, password recovery, advanced edits, tagging/albums.
