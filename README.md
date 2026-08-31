# Car Hub — v0.1 (walking skeleton)

Offline-first in-car entertainment for Android. This is the first installable build:
Home + navigation rail, Owner PIN, CARHUB folder picker (SD-card isolation via Storage
Access Framework), local **movie** and **music** playback, a built-in **game**
(Tic-Tac-Toe), and a screen-pinning **Passenger Mode**. Maps and Kids are placeholders
for the next build.

**No internet, no accounts, no cloud, no ads.** The app declares *no* INTERNET permission.

---

## How to get the APK

You cannot build an APK without an Android toolchain (Java + Android SDK). Pick ONE route.

### Route A — Build in the cloud with GitHub Actions (no installs on your PC)

1. Create a free account at github.com.
2. Create a new repository (e.g. `car-hub`). **Public** is simplest and free.
3. Upload **the contents of this `CarHub/` folder** to the repo:
   - On the repo page: **Add file → Upload files**, then drag in everything inside
     `CarHub/` (including the hidden `.github` folder — if drag-and-drop hides it,
     use **Add file → Create new file** and type `.github/workflows/build.yml`,
     pasting the contents of that file).
   - Commit.
4. GitHub automatically runs the build. Open the **Actions** tab → the latest run.
5. When it finishes (green tick), scroll to **Artifacts** → download **CarHub-APK**.
   Unzip it to get `app-debug.apk`.
6. Put that APK on the tablet (Google Drive / email / USB) and install it (see below).

> The build uses free GitHub-hosted runners. Internet is used only for this one-time
> build — the installed app runs fully offline.

### Route B — Build on a personal PC with Android Studio

1. Install **Android Studio** (free) on your home PC.
2. **Open** this `CarHub/` folder. Let Gradle sync (it downloads dependencies once and
   creates the Gradle wrapper automatically).
3. **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
4. Click **locate** in the popup to find `app/build/outputs/apk/debug/app-debug.apk`.
5. Transfer to the tablet and install.

---

## Installing on the tablet (no ADB, no cable needed)

1. Copy `app-debug.apk` onto the tablet.
2. Tap it. Android will ask to allow installs from this source → enable
   **"Install unknown apps"** for your file manager/browser, then install.
3. Open **Car Hub**.

## First-time setup (owner)

1. Create an **Owner PIN**.
2. **Owner → Manage Content → Select CARHUB folder** and pick (or create) a folder,
   e.g. `Internal storage/CARHUB` or `SD card/CARHUB`.
3. Put media inside it, for example:
   ```
   CARHUB/Movies/Telugu/MyMovie.mp4
   CARHUB/Music/Telugu/Song.mp3
   ```
4. Back in Manage Content, tap **Rescan**. Movies/Music now appear.

## Using Passenger Mode

- Owner Home → **Start Passenger Mode**. The app pins itself (screen pinning).
- For a real lock, enable Android's **"Ask for PIN before unpinning"**
  (Settings → Security → App pinning). Then leaving Car Hub needs the device lock.
- To exit inside the app: rail → **Exit** → enter Owner PIN.

---

## Honest limitations of v0.1

- **Lockdown is Tier B** (screen pinning), because the device is not provisioned as
  Device Owner. The app shows its tier in Owner → Settings. Full escape-proof kiosk
  (Tier A) needs one-time Device Owner provisioning (factory-fresh device + ADB/QR).
- **Codecs are device-dependent.** H.264/AAC in MP4 is safest. HEVC/MKV depends on the
  tablet's hardware decoders.
- **Metadata scan** reads durations file-by-file; very large libraries scan slowly.
  Background indexing + a Room database come in v0.2.
- Music stops when you leave the Music screen (background playback service is v0.2).
- Posters are generated placeholders (real thumbnail extraction is v0.2).
- **Maps** and **Kids** are placeholders in this build.

## Tech

Kotlin · Jetpack Compose (Material3) · Media3/ExoPlayer · DataStore · Storage Access
Framework · DevicePolicyManager/Lock Task. Single module `:app`.
minSdk 26 · targetSdk 34 · Gradle 8.9 · AGP 8.5.2 · Kotlin 2.0.21.
