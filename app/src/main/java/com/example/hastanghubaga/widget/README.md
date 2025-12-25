# **Widget Data Architecture & Update Strategy**

## **Purpose**

This document defines the **data sources, update triggers, and architectural boundaries** for app widgets.

The goal is to ensure that widgets are:

* **Correct** (reflect meaningful domain state)

* **Efficient** (no heavy computation at render time)

* **Robust** (work across process death, reboot, and OS constraints)

* **Maintainable** (clear ownership, no hidden coupling)

---

## **Core Constraints (Android Reality)**

1. **Widgets are not lifecycle-aware**

    * No long-running coroutines

    * No continuous `Flow.collect`

    * No dependency on in-memory state

2. **Widgets cannot query system schedulers**

    * `AlarmManager` is write-only

    * “Next alarm” must be tracked by the app, not the OS

3. **Widgets must be cheap**

    * Limited execution time

    * Often updated from background contexts (Worker / Receiver)

    * Should not run domain logic or DB joins at render time

---

## **Data Classification**

Widget-related data falls into **three distinct categories**:

### **1\. Authoritative Data (Source of Truth)**

Examples:

* Meals

* Supplements

* Activities

* Upcoming schedules

**Characteristics**

* Used in business logic

* Must be consistent and transactional

* Changes frequently and incrementally

**Storage**

* `Room` database only

**Widget access**

* ❌ Widgets must never read this directly

---

### **2\. Computed Projections (Business Derivations)**

Examples:

* Next upcoming alert

* Daily macro totals

* Nutrient aggregates

* % of goal completion

**Characteristics**

* Derived from authoritative data

* Potentially expensive to compute

* Used by multiple consumers (UI, widget, notifications)

**Ownership**

* Domain `UseCase`s

---

### **3\. Widget Snapshot (Display State)**

Examples:

* “Up Next: Creatine @ 7:00 AM”

* Calories today: 1,430

* Protein: 82% of goal

* Key nutrient percentages

**Characteristics**

* Read-only

* Aggregated

* Display-ready

* Safe to be slightly stale

**Storage**

* `SharedPreferences` or `DataStore`

**Widget access**

* ✅ Widgets read *only* this

---

## **Single Source of Truth Rule**

| Layer | Responsibility |
| ----- | ----- |
| Room | Authoritative data |
| UseCases | Compute projections |
| Widget Snapshot | Cached display state |
| Widget UI | Render snapshot only |

Widgets never:

* Run domain logic

* Query Room

* Observe Flows

* Infer state from system APIs

---

## **Widget Snapshot Strategy**

Widgets read from a **cached snapshot**, updated only when **domain meaning changes**.

### **Why a snapshot?**

* Widgets must survive process death

* Widgets must render quickly

* Widgets cannot safely recompute aggregates

* Snapshot decouples widget from domain complexity

---

## **Update Triggers (Canonical)**

Widget snapshot updates are **event-driven**, not time-driven.

### **“Up Next” Snapshot Updates**

Update when:

* A scheduled supplement alert fires

* A supplement is marked taken / undone

* The upcoming schedule is rebuilt (time or config change)

No other events affect “Up Next”.

---

### **Aggregate Snapshot Updates**

Update when:

* A meal is logged / edited / deleted

* A supplement is taken / undone

* An activity is logged / edited / deleted

No periodic refresh is required.

---

## **Explicit Non-Triggers (By Design)**

The widget snapshot **does NOT update** on:

* App resume

* Screen navigation

* Time passing (unless an alert fires)

* Background polling

* Periodic timers

This avoids:

* Battery drain

* Redundant writes

* Inconsistent widget state

---

## **End-to-End Flow (Conceptual)**

`[Domain Event Occurs]`  
`(meal logged / supplement taken / alert fired)`  
`↓`  
`[UseCase Executes]`  
`↓`  
`[Compute Widget-Relevant Projections]`  
`↓`  
`[Persist WidgetSnapshot to Prefs/DataStore]`  
`↓`  
`[Enqueue Widget Update Worker]`  
`↓`  
`[Widget Reads Snapshot and Renders]`

---

## **Worker Interaction Model**

Widgets are updated via:

* `WorkManager` (recommended)

* Short-lived, deterministic work

Workers:

* Call `ObserveNextAlertUseCase().first()`

* Read aggregate snapshots

* Update widget UI once

* Exit

Workers never:

* Collect flows continuously

* Schedule alarms

* Modify domain state

---

## **Rationale for Design Choices**

### **Why not query Room from the widget?**

* Widgets are not lifecycle-safe

* Heavy queries risk ANRs

* Domain logic duplication is inevitable

### **Why not compute aggregates on demand?**

* Expensive joins

* Inconsistent results across consumers

* Hard to test and reason about

### **Why not poll periodically?**

* Unnecessary battery cost

* No new information most of the time

* Violates event-driven architecture

### **Why preferences / DataStore?**

* Fast, process-independent access

* Designed for small, structured state

* Ideal for cached projections

---

## **Scalability & Future Extensions**

This model supports:

* Multiple widget sizes

* Weekly / monthly aggregate widgets

* Wear OS widgets

* Notification previews

* Debug tooling (“next alert” inspector)

Without changing:

* Domain logic

* Scheduler logic

* Database schema

---

## **Summary (Design Contract)**

* Widgets render **snapshots**, not truth

* Snapshots update **only on meaningful domain events**

* Domain logic lives in **UseCases**

* Widgets remain **passive and deterministic**

This contract must be preserved as the widget feature evolves.

---

**End of document**

