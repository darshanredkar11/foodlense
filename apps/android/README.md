# FoodLense Android

Native Android client.

Stack: Kotlin, Jetpack Compose, CameraX, ML Kit adapters, Room, Coroutines/Flow.

Platform-specific code stays here; domain concepts live in `shared/domain` and stable capability interfaces in `shared/contracts`.

## Phase 1 — Logic core

```text
CameraX frame
    │
    ├── BarcodeScanner ──> deterministic barcode result
    │
    └── TextScanner ─────> OCR fallback
                │
                ▼
         ScanCoordinator
                │
                ▼
           ScanResult
```

Barcode recognition intentionally takes precedence over OCR when both produce a result. This keeps product identification deterministic whenever a machine-readable identifier is available.

Platform adapters come after the core behavior is established and tested.
