# Car Hub

Offline-first, in-car entertainment for Android. One APK, one-time setup, ₹0 running
cost, no cloud, no accounts, no ads, no tracking. Everything plays from a folder you
choose; the app declares **no INTERNET permission**.

> Build the app in the cloud (free) or on a PC, copy the APK to the device, and use it
> fully offline forever.

---

## What's in the current build (v0.2b-ui)

**Experience**
- Cinematic dark UI with a glass navigation rail, animated splash, brand play-mark
  logo, gradient feature tiles, and a custom gradient app icon
- Landscape + portrait (auto-rotate)
- Battery %/charging, Bluetooth state, and a clock in the home status cluster

**Movies**
- Poster grid with **real thumbnails extracted from each video**
- Category tabs (from your folders), search, **Favorites**, **Continue Watching**
  (resumes where you left off)
- **Multi-part movies** (`Movie.part1.mkv` + `Movie.part2.mkv`, or CD1/CD2) are grouped
  and played back-to-back as one film
- **Subtitles**: drop `Movie.srt`/`.vtt`/`.ass` next to the video, or pick an embedded
  track from the player's CC button
- Clear "format not supported" message instead of a black screen when a codec is missing

**Music**
- Album art from file tags, browse + search, keeps playing across screens with a
  now-playing bar

**Games**
- Built-in offline Tic-Tac-Toe (no network, no ads, no accounts)

**Owner / Passenger**
- Owner PIN, one-time **CARHUB folder** pick via Storage Access Framework (only that
  folder is ever indexed — SD-card/personal files stay private)
- Passenger Mode locks into Car Hub via Android screen pinning
- Owner Content Manager (counts, rescan)

**Security**
- `allowBackup=false`, least-privilege manifest, local-only state

### In progress (other work stream)
- Exit hardening: corner lock (long-press) → PIN keypad → **pattern** (no biometric)
- **Kids Mode** (owner picks kid-safe folders)
- **Offline Maps** (in-app OSM via Mapsforge/MapLibre — view + position first)
- Security hardening pass: salted + stretched PIN/pattern with lockout

---

## Get the APK (no tools on your PC)

**Cloud build via GitHub Actions (recommended)**
1. The project lives in a GitHub repo with `.github/workflows/build.yml`.
2. Every push builds automatically. Open the repo's **Actions** tab.
3. Click the latest successful run → scroll to **Artifacts** → download **CarHub-APK**.
4. Unzip it → `app-debug.apk` is inside. (GitHub always wraps artifacts in a `.zip`;
   the real installer is the `.apk` inside — extract, don't rename.)

**Or build on a personal PC**
- Install Android Studio (free) → Open this folder → **Build ▸ Build APK(s)**.
- Output: `app/build/outputs/apk/debug/app-debug.apk`.

## Install on the phone/tablet (no ADB, no cable)
1. Copy `app-debug.apk` to the device (Drive / email / USB).
2. Tap it → allow **"Install unknown apps"** for your file manager → **Install**.
3. Open **Car Hub**.

## First-time setup
1. Create an **Owner PIN**.
2. **Owner → Manage Content → Select CARHUB folder**, e.g. `Internal storage/CARHUB`.
3. Add media, for example:
   ```
   CARHUB/Movies/Telugu/MyMovie.mp4
   CARHUB/Movies/Telugu/MyMovie.srt      (optional subtitles)
   CARHUB/Music/Telugu/Song.mp3
   ```
4. Back in Manage Content → **Rescan**.

## Passenger Mode (locking passengers in)
- Owner Home → **Start Passenger Mode**.
- For a real lock on a normal device, first enable Android's
  **Settings → Security → App pinning**, and turn on **"Ask for PIN before unpinning"**.
- Exit: the corner lock → Owner PIN (pattern step is being added).

---

## Honest limitations
- **Lockdown is Tier B** (screen pinning) unless the device is provisioned as **Device
  Owner** (Tier A — needs a factory-fresh device + one-time ADB/QR). The app shows its
  tier in Owner → Settings and never claims a stronger lock than it has.
- **Codec support is device-dependent.** H.264/AAC in MP4 and MP3 play everywhere;
  HEVC/H.265 and some MKV audio depend on the device's decoders.
- **Offline routing/navigation** (turn-by-turn) is the hardest zero-cost piece and is
  planned after offline map viewing + positioning.
- Very large libraries scan more slowly (metadata is read per file); background indexing
  is a later improvement.

## Tech
Kotlin · Jetpack Compose (Material3) · Media3/ExoPlayer · Storage Access Framework ·
DataStore · DevicePolicyManager/Lock Task. minSdk 26 · targetSdk 34 · Gradle 8.9 ·
AGP 8.5.2 · Kotlin 2.0.21. No backend, no analytics, no third-party trackers.
