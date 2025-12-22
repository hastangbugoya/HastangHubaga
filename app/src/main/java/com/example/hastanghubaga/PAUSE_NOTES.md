# 🛑 PAUSE_NOTES.md — HastangHubaga

> This document records the **intentional pause state** of the project.
> It exists so future-me can resume work **without re-deriving context,
> decisions, or tooling constraints**.

Last updated: **2025-12-20**

---

## 1. Project State Summary

The project is in a **stable, buildable state**.

- `./gradlew clean build` succeeds
- No unresolved Gradle or dependency errors
- Some warnings exist but are **understood and acceptable**
- No half-migrated systems are left behind

This pause is **intentional**, not forced.

---

## 2. Toolchain (IMPORTANT)

### Android / Gradle
- **Android Gradle Plugin**: `8.9.1`
- **Gradle Wrapper**: `8.10.x`
- **compileSdk**: `36`
- **targetSdk**: unchanged
- **minSdk**: unchanged

> AndroidX libraries (Activity 1.11, Core 1.17, Material3 1.4, Compose runtime)
> now enforce minimum AGP versions via AAR metadata.
> Upgrading AGP was required, not optional.

### Kotlin
- **Kotlin version**: `1.9.24`
- **NOT migrated to Kotlin 2.0**
- **KAPT still in use** (KSP migration deferred)

---

## 3. Version Catalog Rules (libs.versions.toml)

Key conventions established and now enforced:

- **TOML keys define Kotlin accessors**
- Hyphens (`-`) become camelCase
- **Avoid `.test.` segments** in keys to prevent collision with source sets

Example:
- ❌ `androidx-compose-ui-test-manifest`
- ✅ `androidx-compose-ui-testManifest`

If adding new dependencies later, follow this rule strictly.

---

## 4. Time & Date Architecture (CRITICAL DESIGN DECISION)

Time handling is **intentionally split by layer**:

### Domain / UI / ViewModel layers
- Use **`kotlinx.datetime` ONLY**
- Types: `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`
- No `java.time` usage here

### Repository / Data / System layers
- Convert to **`java.time`** only when required:
    - Room
    - AlarmManager
    - System APIs

### TimePolicy
- Centralized conversion utilities
- Explicit boundary between Kotlin time and Java time
- Clock injected for testability

> If something feels awkward with time handling later,
> **do not mix time libraries across layers** — fix the boundary instead.

---

## 5. Today Timeline Feature — Status

### Implemented
- Domain `TimelineItem` model exists
- UI `TimelineItemUiModel` exists
- Supplements / Meals / Activities are normalized
- Timeline build logic works
- Tap → resolve → effect → confirm → use case → repository flow works

### Intentionally incomplete (design decisions pending)
- Actual vs scheduled dose time UX
- Whether timeline items should carry full datetime or display-only time
- Sleep semantics (activity vs sentinel)
- Timeline repository abstraction (currently built in ViewModel)

None of these block compilation or correctness.

---

## 6. Dose Logging Flow (Current Design)

- UI emits `ConfirmDose`
- Use case validates input
- Time resolution uses `TimeUseIntent`
- Repository persists resolved date + time

Design allows future extension for:
- Actual time override
- Backdating
- Corrections

---

## 7. Alerts Package — Intentional Pause

The alerts system is **scaffolded but dormant**.

### Present
- `SupplementAlertService`
- `SupplementAlertScheduler`
- `SupplementAlertReceiver`
- `BootCompletedReceiver`

### Not finalized
- Alert rules
- Scheduling frequency
- Snooze behavior
- User preferences

All alert classes are heavily KDoc’d to preserve intent.

> Alert behavior is a **product decision**, not a technical blocker.

---

## 8. Dependency Injection

- Hilt wiring is correct
- `Clock` is injected
- `FakeClock` exists for tests
- No missing bindings

Possible future improvement:
- Migrate Clock / TimePolicy into a dedicated domain module

---

## 9. Tests

- Timeline tests pass
- No Android framework calls in JVM tests
- `LogSupplementDoseUseCase` test planned but not required before pause

---

## 10. How to Resume Later (Checklist)

1. Run:
   ```bash
   ./gradlew clean build
