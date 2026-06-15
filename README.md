<div align="center">

  <h1>ArchiveTune Core</h1>

  <p align="center">
    <strong>InnerTube API client for YouTube Music.</strong>
    <br />
    <em>The core library powering <a href="https://github.com/ArchiveTuneApp/ArchiveTune">ArchiveTune</a> — a high-performance, privacy-focused YouTube Music client for Android.</em>
  </p>

  <p align="center">
    <img src="https://img.shields.io/github/v/release/ArchiveTuneApp/core?style=for-the-badge&color=6366f1&labelColor=1e1e2e&logo=github" alt="Latest Version" />
    <img src="https://img.shields.io/github/license/ArchiveTuneApp/core?style=for-the-badge&color=6366f1&labelColor=1e1e2e" alt="License" />
    <img src="https://img.shields.io/badge/Language-Kotlin-7f52ff?style=for-the-badge&logo=kotlin&color=6366f1&labelColor=1e1e2e" alt="Kotlin" />
    <img src="https://img.shields.io/badge/Runtime-JVM-6366f1?style=for-the-badge&logo=openjdk&labelColor=1e1e2e" alt="JVM" />
  </p>

</div>

## Overview

This is the standalone InnerTube API core extracted from [ArchiveTune](https://github.com/ArchiveTuneApp/ArchiveTune). It provides a complete Ktor-based HTTP client for interacting with YouTube Music's InnerTube API, including request signing, response parsing, proxy rotation, and playback authentication.

## Features

- **Full API Coverage** — search, browse, library, playlist management, playback, and account interactions
- **Ktor Client** — built on Ktor with OkHttp engine, content negotiation, brotli encoding, and DNS-over-HTTPS
- **Response Parsing** — complete set of Kotlinx Serialization models for InnerTube responses
- **Page Parsers** — domain-level parsers that transform raw JSON into typed page objects
- **Proxy Rotation** — built-in rotating proxy selector with cooldown tracking for failed proxies
- **Playback Auth** — PO token management for authenticated playback
- **NewPipe Integration** — optional cipher deobfuscation and stream URL extraction via NewPipe Extractor

## Package Structure

```
moe.rukamori.archivetune.innertube/
├── InnerTube.kt              — Core HTTP client
├── YouTube.kt                — High-level API singleton (main entry point)
├── MusicBackend.kt           — API contract interface
├── PlaybackAuthState.kt      — Authentication state model
├── SearchFilter.kt           — Search filter parameter helpers
├── LibraryFilter.kt          — Library filter parameter helpers
├── models/                   — Response data models (JSON deserialization targets)
│   ├── body/                 — Request body models
│   └── response/             — Response wrapper models
├── pages/                    — Page parsers (response-to-domain transformation)
├── proxy/                    — Proxy rotation and configuration
└── utils/                    — Shared utilities
```

## Dependencies

- **Ktor Client** 3.5.0 — HTTP client core, OkHttp engine, content negotiation, brotli encoding, JSON serialization
- **OkHttp** 5.3.2 — DNS-over-HTTPS support
- **Kotlinx Serialization** — JSON deserialization
- **NewPipe Extractor** 0.26.2 — stream URL extraction, cipher deobfuscation, Bandcamp/SoundCloud search
- **re2j** 1.8 — Google RE2 regular expressions
- **Rhino** 1.9.1 — JavaScript engine (cipher operations)

## Usage

```kotlin
// Search
val results = YouTube.search("query", SearchFilter.SONGS)

// Browse
val home = YouTube.home()
val album = YouTube.album("browse_id")
val artist = YouTube.artist("browse_id")
val playlist = YouTube.playlist("playlist_id")

// Player
val player = YouTube.player("video_id", "playlist_id", YouTubeClient.WEB)

// Playlist management
YouTube.createPlaylist("My Playlist", "description")
YouTube.addToPlaylist("playlist_id", listOf("video_id"))

// Auth
YouTube.authState = PlaybackAuthState(cookie = "...", visitorData = "...")
```

## License

[GNU General Public License v3.0](LICENSE)
