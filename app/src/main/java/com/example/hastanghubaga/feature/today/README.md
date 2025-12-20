```md
# Today Screen – Timeline Intake Flow

This document describes the flow for handling timeline item taps,
specifically supplement intake logging, using the MVI + Clean Architecture
pattern used in this app.

---

## Timeline Item Tap → Supplement Intake Logging

The flow below shows how a user tap on a timeline item results in
a validated supplement intake being logged to the database.

All layers have clearly separated responsibilities.

```text
TimelineRow (Composable)
  ↓ onClick(item: TimelineItemUiModel)

TodayScreenContract.Intent.TimelineItemClicked
  ↓

TodayScreenViewModel.onIntent(...)
  ↓

HandleTimelineItemTapUseCase
  ↓ resolve(item)

TimelineTapAction.RequestDoseInput
  ↓

TodayScreenContract.Effect.ShowDoseInputDialog
  ↓

DoseInputDialog (Composable)
  ↓ onConfirm(dose, unit)

TodayScreenContract.Intent.ConfirmDose
  ↓

LogSupplementDoseUseCase
  ↓

SupplementLogRepository.upsertDoseLog(...)

---

## Responsibilities by Layer

### UI Layer
- `TimelineRow`
- `DoseInputDialog`

Responsibilities:
- Render UI
- Capture user interaction
- Collect user input
- Emit intents

No business logic or persistence.

---

### ViewModel Layer
- `TodayScreenViewModel`
- `TodayScreenContract.Intent`
- `TodayScreenContract.Effect`

Responsibilities:
- Route intents
- Launch use cases
- Emit UI effects

No validation or database access.

---

### Domain Layer
- `HandleTimelineItemTapUseCase`
- `LogSupplementDoseUseCase`
- `TimelineTapAction`

Responsibilities:
- Decide what input is required
- Validate user input
- Define domain actions

No UI or Room dependencies.

---

### Data Layer
- `SupplementLogRepository`

Responsibilities:
- Persist intake logs
- Handle time policy (UTC vs local)
- Perform upserts

No UI or domain logic.

---

## Notes

- Meal and Activity timeline items are currently no-ops.
- This flow intentionally requires explicit user confirmation
  before logging supplement intake.
- Additional features (confirmation, undo, analytics) can be
  added without changing this structure.

---

```mermaid
sequenceDiagram
  participant UI
  participant VM
  participant UseCase
  participant Repo

  UI->>VM: TimelineItemClicked
  VM->>UseCase: resolve(item)
  UseCase-->>VM: RequestDoseInput
  VM-->>UI: ShowDoseInputDialog
  UI->>VM: ConfirmDose
  VM->>UseCase: logDose
  UseCase->>Repo: upsertDoseLog
