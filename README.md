HastangHubaga

Overview

HastangHubaga is a Jetpack Compose Android application designed for
health management, including supplement timing, meal tracking, and daily
scheduling. The app uses MVVI architecture, Hilt 2, WorkManager,
Services, Widgets, and a dark/light theme system.

1. App Structure

MainActivity Scaffold

-   Top App Bar with title “Today’s Schedule”
-   Bottom Navigation:
    1.  Today
    2.  Calendar
    3.  Data
-   Bottom Sheet for quick actions or details

------------------------------------------------------------------------

2. Screens

A. Today Screen (Dashboard)

Displays: - Hour 0 setup and display - Next supplement card with
countdown - Timeline list for supplements and meals - Floating Action
Button for adding supplements or meals

B. Calendar Screen

Monthly calendar displaying logged meals and supplements. Tapping a date
opens a bottom sheet with details.

C. Data Management Screen

Tabs: - Supplements: CRUD, components, dose, offsets - Meals: CRUD,
macros tracking - Workouts: Placeholder

------------------------------------------------------------------------

3. Widgets, Services, Alerts

Widgets

-   Next Dose Widget
-   Quick Meal Entry Widget

Services

-   Background timing service for supplement schedules

WorkManager

-   Recalculates schedules and manages alerts daily

Alerts

-   Push notifications + smartwatch integration

------------------------------------------------------------------------

4. Theming

Light and dark themes with placeholder color palettes.

------------------------------------------------------------------------

5. Navigation Structure

MainActivity ├─ TodayScreen
│ ├─ Hour0Dialog
│ ├─ SupplementDetailSheet
│ └─ AddMealSheet
├─ CalendarScreen
│ └─ DayDetailSheet
└─ DataScreen
├─ SupplementsTab
├─ MealsTab
└─ WorkoutsTab

------------------------------------------------------------------------

6. Future Enhancements

-   Stats & analytics
-   Barcode scanning
-   Google Fit / Samsung Health sync
-   Cloud sync support
