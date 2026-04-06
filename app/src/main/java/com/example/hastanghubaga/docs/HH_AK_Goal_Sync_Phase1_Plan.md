# HH ⇄ AK Goal Sync Plan (Phase 1)
_Date: 2026-04-06_

## Scope

This plan covers the **first sync phase** for nutrition goals between:

- **AdobongKangkong (AK)**
- **HastangHubaga (HH)**

This phase is intentionally **AK → HH only**.

HH will:

1. fetch AK monthly summary data
2. also query AK goal data
3. compare imported AK goals against HH local goals
4. inform the user that AK has **new** or **changed** goals
5. show a **side-by-side comparison**
6. always ask:

> **Do you want to import?**

There is **no automatic import** in this phase.

This gives us time to:
- validate the payload shape
- verify overlap rules
- test conflict behavior
- refine the CRUD and link model
- avoid silent goal drift across apps

---

# 1. Why this phased approach is good

This is the right first step because it keeps the system:

- transparent
- reversible
- debuggable
- user-controlled

It also avoids early mistakes with automatic sync, especially while:
- goal schemas are still evolving
- nutrient overlap rules are still being defined
- HH and AK may not have identical goal models yet

---

# 2. Sync Direction for Phase 1

## Direction
**AK → HH only**

HH is the consumer in this phase.

AK remains the source that HH checks against.

## Trigger
When HH performs the monthly summary fetch flow, HH should also:

1. request AK nutrition goals
2. compare AK goals with HH local goals
3. surface changes to the user

## Import rule
HH never auto-applies imported goal changes in this phase.

The user must explicitly confirm import.

---

# 3. Phase 1 Behavior

## High-level user experience

When HH fetches monthly summary data:

1. HH fetches monthly summary from AK
2. HH fetches AK goal payload
3. HH compares AK payload against HH local goal plans
4. If no meaningful differences exist:
   - no action needed
5. If AK has new or changed goals:
   - HH shows a comparison UI
   - HH asks whether to import

### Prompt style
Use a confirmation pattern such as:

> AK has new or changed nutrition goals.  
> Do you want to import them into HH?

---

# 4. Important Rule: No Auto Import

This must remain explicit in Phase 1.

## Why
Auto-import is risky because:
- users may have active HH-only edits
- linked plans may not be established yet
- not all nutrients overlap
- min/target/max merge behavior may still evolve
- side-by-side visibility is critical while ironing out edge cases

## Phase 1 import rule
- detect only
- compare only
- prompt only
- import only on user confirmation

---

# 5. Goal Model Direction

HH should move away from a single wide row of fixed target columns and toward a plan-based model with explicit nutrient goal rows.

## Recommended structure

### A. Nutrition plan
Plan-level metadata:
- id
- name
- type
- start date
- end date
- active state
- source metadata
- sync metadata

### B. Nutrition plan goal rows
One row per nutrient per plan:
- nutrient key
- min
- target
- max

### C. Optional import / link metadata
Used to compare and track imported AK-origin plans.

This model supports:
- multiple active plans
- overlapping nutrient sync
- side-by-side comparison
- future manual linking
- future semi-automation

---

# 6. Multiple Active Plans Rule

HH will allow multiple plans to be active at the same time.

For overlapping nutrients:

- **effective min** = highest min
- **effective max** = lowest max

This gives the tightest valid overlap across active plans.

## Conflict rule
If:

```text
effective min > effective max
```

then the active plans conflict for that nutrient.

HH should never silently ignore that.

This should surface as a conflict in local plan resolution and also during AK comparison/import.

---

# 7. Target Handling

For now:

- min/max are the primary conflict and range values
- target is advisory
- if targets differ across active plans, HH may:
  - keep them per-plan
  - show them side-by-side
  - derive no single effective target unless explicitly defined later

For import comparison, HH should compare all three values:
- min
- target
- max

But local effective range resolution should prioritize:
- min
- max

---

# 8. Overlapping Nutrients Only

Sync should apply **only to nutrients supported by both apps**.

## Example
If AK supports:
- protein
- carbs
- fat
- calories
- sodium
- potassium

and HH supports:
- protein
- carbs
- fat
- calories
- sodium
- fiber

then shared overlap is only:
- protein
- carbs
- fat
- calories
- sodium

### Result
- potassium remains AK-only
- fiber remains HH-only

HH should not invent mappings for unsupported nutrients.

---

# 9. Canonical Nutrient Keys

Comparison and import must use canonical nutrient keys rather than display labels.

Examples:
- `CALORIES_KCAL`
- `PROTEIN_G`
- `CARBS_G`
- `FAT_G`
- `SODIUM_MG`
- `CHOLESTEROL_MG`
- `FIBER_G`

This avoids brittle matching based on UI strings.

---

# 10. Comparison Model in HH

HH should compare AK goal payloads against local plans using a dedicated comparison layer.

## Comparison categories

### A. New in AK
AK has a plan that HH does not have locally.

### B. Changed in AK
AK has a plan that corresponds to a local HH plan, but values differ.

### C. Local only
HH has a local plan with no AK counterpart.

### D. Same
AK and HH effectively match.

---

# 11. Side-by-Side Comparison UI Requirements

Before import, HH should show:

- AK plan name
- HH local plan name
- date ranges
- active state
- overlapping nutrient values
- differences per nutrient
- new / changed / same markers

## Per nutrient comparison row
For each overlapping nutrient:

- AK min / target / max
- HH min / target / max
- status:
  - same
  - changed
  - missing locally
  - unsupported locally

This UI should make import understandable before any data mutation.

---

# 12. Matching Strategy for Phase 1

Phase 1 should not assume that plans are the same just because names are similar.

## Recommended matching order

### 1. Existing explicit link
If HH already has stored sync metadata / imported source info, use that first.

### 2. Deterministic external source identity
If AK exposes a stable plan id, HH should preserve that.

### 3. Suggested match only
If no stable link exists, HH may suggest a likely match based on:
- similar name
- overlapping date range
- similar nutrient values

But Phase 1 should still let the user choose whether to import.

---

# 13. Import Semantics for Phase 1

When the user confirms import:

## Preferred behavior
Import AK plan data into HH in a controlled way.

### Good first behavior
- create new HH plan from AK payload
- or update the matching HH imported-linked plan
- preserve source metadata indicating AK origin

## Important
HH should distinguish:
- locally-authored HH plans
- AK-imported plans

This distinction will help later when semi-automation arrives.

---

# 14. Recommended HH Data Model Changes

## Replace / evolve old wide-goal row model

Current HH goal storage is too fixed-width for the direction we want.

HH should move toward:

### `nutrition_plans`
Plan metadata

Suggested fields:
- `id`
- `name`
- `type`
- `startDate`
- `endDate`
- `isActive`
- `sourceApp`
- `sourcePlanId`
- `createdAt`
- `updatedAt`

### `nutrition_plan_goals`
One nutrient goal row per plan

Suggested fields:
- `id`
- `planId`
- `nutrientKey`
- `minValue`
- `targetValue`
- `maxValue`

### optional later: `nutrition_plan_links`
For explicit cross-app linking if needed separately from imported source metadata.

---

# 15. Repository / CRUD Scope for HH

Before wiring sync UI, HH should have complete local CRUD support for the new plan model.

## CRUD should support:
- create plan
- update plan metadata
- delete plan
- activate / deactivate plan
- list plans
- get plan by id
- save nutrient goal rows
- read nutrient goal rows for a plan

## Also needed:
- get all active plans
- resolve effective nutrient ranges from active plans
- compare imported AK plans against local HH plans

---

# 16. Comparison / Import Use Cases to Add Later

These are not wiring tasks yet, but they should shape the data model.

## Likely use cases
- `GetNutritionPlansUseCase`
- `GetNutritionPlanByIdUseCase`
- `CreateNutritionPlanUseCase`
- `UpdateNutritionPlanUseCase`
- `DeleteNutritionPlanUseCase`
- `SetNutritionPlanActiveUseCase`
- `ResolveEffectiveNutritionGoalsUseCase`
- `CompareImportedAkNutritionGoalsUseCase`
- `ImportAkNutritionGoalsUseCase`

Phase 1 wiring only needs the comparison and import behavior later, but the CRUD layer should prepare for them now.

---

# 17. Suggested Import Metadata in HH

Each HH plan that came from AK should preserve enough metadata to support later comparisons.

Recommended:
- `sourceApp = AK`
- `sourcePlanId = stable AK plan id`
- `lastImportedAt`
- possibly `sourceLastModifiedAt` if available

This makes future “changed in AK” detection much safer.

---

# 18. Monthly Summary + Goal Query Flow in HH

## Planned Phase 1 flow

```text
HH requests AK monthly summary
        +
HH requests AK nutrition goals
        ▼
HH stores / parses payload
        ▼
HH compares AK goals to local HH plans
        ▼
if differences exist:
    show side-by-side comparison
    ask user "Do you want to import?"
else:
    no action needed
```

---

# 19. Future Phase Direction

Later, after Phase 1 is stable, we may add:

## A. Better link management
- explicit local ↔ external plan linking
- relink UI

## B. Semi-automated AK querying
HH could periodically check AK goals, but still prompt before import.

## C. Conflict handling
If HH and AK both become editable peers later, compare nutrient-level changes and ask the user which side wins.

## D. More automated import suggestions
Still user-approved, not silent.

---

# 20. What HH Should Build Now

## Build now
- new local plan DB model
- nutrient-row goal model
- CRUD support
- active-plan resolution
- imported-source metadata support
- comparison-ready repository surface

## Do not build yet
- automatic import
- automatic merge
- silent overwrite
- two-way auto-sync
- background sync enforcement

---

# 21. Final Recommendation

This Phase 1 design is strong because it is:

- user-controlled
- safe
- extensible
- compatible with future semi-automation
- consistent with planner-first architecture

The key rule to preserve is:

> **HH may detect AK goal changes automatically, but it must always ask before importing in Phase 1.**

