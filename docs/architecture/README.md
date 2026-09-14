# FoodLense Architecture

FoodLense is intentionally structured as a multi-client monorepo.

## Client boundaries

Each client owns presentation, platform integration, and platform-specific adapters. Shared code contains stable domain concepts and contracts only.

```text
                +----------------------+
                |   Shared contracts   |
                +----------+-----------+
                           |
          +----------------+----------------+
          |                |                |
     +----v----+      +----v----+      +----v----+
     | Android |      |   iOS   |      |   Web   |
     +---------+      +---------+      +---------+
```

### Android

```text
UI (Compose)
   -> Feature / Use Cases
      -> Domain contracts
         -> platform adapters
            -> CameraX / ML Kit / Room
```

Domain logic must not depend directly on Android APIs.
