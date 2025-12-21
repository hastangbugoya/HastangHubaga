# Alerts Package (`alerts`)

This package contains the **infrastructure-level implementation** for
supplement reminder alerts.

It is intentionally **not feature-scoped** and does not contain UI logic.
Its sole responsibility is to bridge **domain scheduling decisions** to
**Android system alarms and notifications**.

---

## High-Level Goal

> Notify the user when a supplement is due, at the correct time, using
> Android system alarms — without duplicating scheduling logic or
> leaking time calculations into UI or feature layers.

This package does **not** decide *when* a supplement should be taken.
That decision lives in the **domain + repository layer**.

---

## Core Design Principles

### 1. Absolute Time Only (`Instant`)
All alert scheduling operates exclusively on:

**Why:**
- `Instant` represents an absolute moment in time
- AlarmManager ultimately requires epoch milliseconds
- Time zones and DST are resolved *before* reaching this layer

❗ This package should **never** use:
- `LocalDate`
- `LocalTime`
- `ZonedDateTime` (except as a temporary conversion boundary)

---

### 2. One-Way Data Flow

There is **no feedback loop** from alerts back into domain logic.

---

### 3. Infrastructure, Not Feature Code

This package is **app-level infrastructure**:

- long-lived
- system-triggered
- cross-feature
- independent of UI lifecycles

It should **never** live under a `feature.*` package.

---

## Components

### `SupplementAlertScheduler`

Responsible for:
- querying the repository for active supplements
- asking for the *next absolute dose time*
- scheduling alarms for the next 24 hours

Key rules:
- Supplements with `DoseAnchorType.ANYTIME` are ignored
- Only alarms within a rolling 24-hour window are scheduled
- No calendar math is performed here — only comparisons of `Instant`

See implementation in:
- `SupplementAlertScheduler.kt` :contentReference[oaicite:0]{index=0}

---

### `SupplementAlertReceiver`

A lightweight `BroadcastReceiver` triggered by AlarmManager.

Responsibilities:
- receive the alarm intent
- forward the intent payload to `SupplementAlertService`
- start the service as a **foreground service** (required on modern Android)

This class does **not**:
- build notifications
- perform scheduling
- query repositories

See implementation in:
- `SupplementAlertReceiver.kt` :contentReference[oaicite:1]{index=1}

---

### `SupplementAlertService`

The execution boundary for alerts.

Responsibilities:
- run in a foreground context
- prepare and show notifications
- perform any work that requires a Service lifecycle

Important:
- The service assumes the alarm time is already correct
- It does not re-evaluate scheduling rules

(See KDoc on the class itself for detailed rationale.)

---

## Scheduling Rules (Current State)

The **current rules are intentionally minimal**:

- Only supplements with a fixed time (non-ANYTIME) are eligible
- Only the *next* upcoming dose is scheduled
- Only doses within the next 24 hours are considered
- Exact alarms are used (`setExactAndAllowWhileIdle`)
- On Android 12+:
    - `canScheduleExactAlarms()` is checked
    - Failure is silent (UX handled elsewhere)

These rules are **implementation choices**, not permanent decisions.

---

## What This Package Does *NOT* Decide

The following are **explicitly out of scope** here:

- Whether alerts are enabled/disabled globally
- Notification channels or user preferences
- Snoozing behavior
- Repeating alerts
- Hydration or non-supplement reminders
- Rescheduling after dose logging
- Cloud sync implications

Those belong in:
- domain policy
- settings
- or separate infrastructure modules

---

## Expected Future Extensions

This package is intentionally structured so it can evolve to support:

- BOOT_COMPLETED rescheduling
- periodic rescheduling jobs
- cloud-driven schedule changes
- multiple alert types
- user-configurable alert windows

Without changing:
- the `Instant`-based contract
- the separation between domain and infrastructure

---

## Non-Negotiable Rules (Do Not Break)

❌ Do not introduce `java.time` into public method signatures  
❌ Do not compute calendar logic here  
❌ Do not move alert scheduling into feature modules  
❌ Do not reformat or reinterpret Instants  
❌ Do not let UI layers schedule alarms directly

---

Domain / Repository
↓
Instant (next dose)
↓
Alert Scheduler
↓
AlarmManager
↓
BroadcastReceiver
↓
Foreground Service
## TL;DR

- Domain decides **when**
- Alerts package executes **how**
- Instants flow downward
- Alarms are infrastructure
- UI stays out of it

This package is intentionally boring — and that’s a good thing.

There is **no feedback loop** from alerts back into domain logic.

---

### 3. Infrastructure, Not Feature Code

This package is **app-level infrastructure**:

- long-lived
- system-triggered
- cross-feature
- independent of UI lifecycles

It should **never** live under a `feature.*` package.

---

## Components

### `SupplementAlertScheduler`

Responsible for:
- querying the repository for active supplements
- asking for the *next absolute dose time*
- scheduling alarms for the next 24 hours

Key rules:
- Supplements with `DoseAnchorType.ANYTIME` are ignored
- Only alarms within a rolling 24-hour window are scheduled
- No calendar math is performed here — only comparisons of `Instant`

See implementation in:
- `SupplementAlertScheduler.kt` :contentReference[oaicite:0]{index=0}

---

### `SupplementAlertReceiver`

A lightweight `BroadcastReceiver` triggered by AlarmManager.

Responsibilities:
- receive the alarm intent
- forward the intent payload to `SupplementAlertService`
- start the service as a **foreground service** (required on modern Android)

This class does **not**:
- build notifications
- perform scheduling
- query repositories

See implementation in:
- `SupplementAlertReceiver.kt` :contentReference[oaicite:1]{index=1}

---

### `SupplementAlertService`

The execution boundary for alerts.

Responsibilities:
- run in a foreground context
- prepare and show notifications
- perform any work that requires a Service lifecycle

Important:
- The service assumes the alarm time is already correct
- It does not re-evaluate scheduling rules

(See KDoc on the class itself for detailed rationale.)

---

## Scheduling Rules (Current State)

The **current rules are intentionally minimal**:

- Only supplements with a fixed time (non-ANYTIME) are eligible
- Only the *next* upcoming dose is scheduled
- Only doses within the next 24 hours are considered
- Exact alarms are used (`setExactAndAllowWhileIdle`)
- On Android 12+:
    - `canScheduleExactAlarms()` is checked
    - Failure is silent (UX handled elsewhere)

These rules are **implementation choices**, not permanent decisions.

---

## What This Package Does *NOT* Decide

The following are **explicitly out of scope** here:

- Whether alerts are enabled/disabled globally
- Notification channels or user preferences
- Snoozing behavior
- Repeating alerts
- Hydration or non-supplement reminders
- Rescheduling after dose logging
- Cloud sync implications

Those belong in:
- domain policy
- settings
- or separate infrastructure modules

---

## Expected Future Extensions

This package is intentionally structured so it can evolve to support:

- BOOT_COMPLETED rescheduling
- periodic rescheduling jobs
- cloud-driven schedule changes
- multiple alert types
- user-configurable alert windows

Without changing:
- the `Instant`-based contract
- the separation between domain and infrastructure

---

## Non-Negotiable Rules (Do Not Break)

❌ Do not introduce `java.time` into public method signatures  
❌ Do not compute calendar logic here  
❌ Do not move alert scheduling into feature modules  
❌ Do not reformat or reinterpret Instants  
❌ Do not let UI layers schedule alarms directly

---

## TL;DR

- Domain decides **when**
- Alerts package executes **how**
- Instants flow downward
- Alarms are infrastructure
- UI stays out of it

This package is intentionally boring — and that’s a good thing.
