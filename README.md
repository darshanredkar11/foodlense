# FoodLense

FoodLense is a cross-platform food intelligence application built as a monorepo.

## Monorepo

```text
foodlense/
├── apps/
│   ├── android/     # Native Android client
│   ├── ios/         # Native iOS client (planned)
│   └── web/         # Web client (planned)
├── shared/
│   ├── domain/      # Platform-neutral business concepts
│   └── contracts/   # Cross-client capability contracts
├── docs/
│   └── architecture/
└── .github/
```

## Principles

- Native clients first: Android, iOS, and web remain independently optimized.
- Keep domain concepts and contracts platform-neutral where practical.
- Privacy-first: food images and scan data should remain local by default.
- No embedded secrets or long-lived API credentials in client applications.
- Offline-first local persistence.
- Test behavior at boundaries, not only UI rendering.

## Current milestone

Android foundation: Kotlin + Jetpack Compose + CameraX, with barcode/OCR contracts and Room persistence to follow.

## Status

Early development.
