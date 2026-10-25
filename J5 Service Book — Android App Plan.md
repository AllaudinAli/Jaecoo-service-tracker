# J5 Service Book — Android App Plan

Oct 25, 2026 · @HorrizonAI-25

## Overview

Build a native, offline Android app that tracks maintenance and warranty for one car, a Jaecoo J5 SHS hybrid (Pakistan market), using schedule data taken from its owner's manual and warranty booklet. The owner only enters dates and odometer readings; the app works out what is due, when, and sends reminders.

The app replaces a web prototype (the "J5 Service Book" artifact) and keeps its features: next-service countdown, upcoming schedule, distance-based items, service history, warranty status and fluid specs.

**How to use this plan with Claude Code:** export this doc as Markdown, save it as `PLAN.md` in an empty project folder, then start Claude Code there and say: *"Read PLAN.md and build Phase 1."* Work through the phases in order, testing on the phone after each one.

## Tech stack and setup

Use Kotlin with Jetpack Compose, store everything locally with Room, and schedule reminders with WorkManager. There is no backend, login or internet permission.

| Concern | Choice | Why |
| --- | --- | --- |
| Language / UI | Kotlin, Jetpack Compose, Material 3 | Current Android standard; Claude Code knows it well |
| Storage | Room (SQLite) | Offline, survives restarts, easy to export |
| Settings | DataStore (Preferences) | Reminder lead time, units |
| Dates | java.time (`LocalDate`) | minSdk 26 supports it natively |
| Reminders | WorkManager daily worker + notifications | Survives reboots, battery-friendly |
| Architecture | Single module, MVVM (ViewModel + StateFlow), manual DI | Small app; no Hilt needed |
| Build | Gradle Kotlin DSL, version catalog | Android Studio default |
| Tests | JUnit 5 for the schedule engine | The date and km logic must be correct |
| minSdk / targetSdk | 26 / latest stable | Covers almost every phone in use |

Package name: `com.example.j5servicebook` (change if you like).

**Before Phase 1, install once on your computer:**

1. Android Studio (includes the Android SDK and an emulator).
2. Claude Code, then open a terminal in an empty folder containing `PLAN.md`.
3. On your phone: Settings → About phone → tap Build number 7 times, then turn on USB debugging in Developer options.

## Data model

Only three things are stored: the car profile, odometer readings and service records. All schedule and warranty rules are constants in code (a `ScheduleRules.kt` file), so the manual's data never needs typing in.

**Room tables**

| Table | Fields |
| --- | --- |
| `car_profile` (one row) | `id`, `name` ("Jaecoo J5 SHS"), `purchaseDate: LocalDate`, `createdAt` |
| `odometer_reading` | `id`, `date: LocalDate`, `km: Int`, `source` (MANUAL or SERVICE) |
| `service_record` | `id`, `date: LocalDate`, `km: Int`, `kind` (PERIODIC, INSPECTION, REPAIR, OTHER), `itemsDone: Set<ItemId>` (stored as comma-separated text via a TypeConverter), `workshop: String?`, `cost: Int?` (PKR), `jobCardNo: String?`, `notes: String?` |

**Rules (code, not database)**

- `enum class ItemId` — every replaceable item: OIL\_AND\_FILTER, AIR\_FILTER, AC\_FILTER, TIRE\_ROTATION, THROTTLE\_CLEAN, ALIGNMENT, SPARK\_PLUGS, BRAKE\_FLUID, COOLANT, TRANSMISSION\_FLUID, FUEL\_FILTER, CHARCOAL\_CANISTER, FUEL\_FILLER\_HOSE, FUEL\_VAPOR\_PIPE.
- `data class IntervalRule(item, km: Int, months: Int?, label)` — the distance and time items.
- `val PERIODIC_PATTERN` — the 10-visit cycle for air and A/C filters.
- `val EVERY_VISIT_CHECKS: List<String>` and `val FIRST_SERVICE_CHECKLIST: List<String>`.
- `data class WarrantyRule(name, months, km, category)`.

The latest odometer value is the highest `km` across `odometer_reading` and `service_record`.

## Maintenance schedule (owner's manual pp. 226–234)

A periodic service is due every 3 months or 5,000 km, whichever comes first. The table has 10 columns (3 to 30 months, 5,000 to 50,000 km); after the 10th service the cycle repeats from column 1, adding 30 months and 50,000 km. Codes: I = inspect, adjust, clean or replace if necessary; R = replace.

**Filters that change across the 10-service cycle**

| Service # in cycle | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Months / km×1,000 | 3 / 5 | 6 / 10 | 9 / 15 | 12 / 20 | 15 / 25 | 18 / 30 | 21 / 35 | 24 / 40 | 27 / 45 | 30 / 50 |
| Air filter | I | I/R | I | R | I | I/R | I | R | I | I/R |
| A/C cabin filter | I | I/R | R | I | I/R | R | I | I/R | R | I |

**Replaced at every periodic service:** engine oil and oil filter (every 3 months or 5,000 km).

**Inspected at every periodic service (I in every column):** meter/instrument cluster, video and entertainment system, diagnostic tester scan, front and rear wiper blades, wiper system, cooling effect, A/C system, coolant level, coolant freezing point, brake fluid level, brake fluid water content, engine oil level, transmission fluid level, 12V battery voltage, shock absorbers, drive shafts and boots, chassis bolt torque, steering gear, steering column, steering links/ball joints/boots, tyre appearance, tyre tread pattern, tyre inflation pressure, wheel bolt torque, brake pads, brake discs, engine oil pan and drain bolts, transmission housing, fluid lines (steering, brake, fuel) and wire harness, power battery surface (rust/deformation), power battery fixing bolts, power battery waterproof vent valve and vent hole, power battery connector (dust, water, aging), low-voltage harness connectors, high-voltage circuit connectors and harness.

**Distance and time items** (counted from the last time each was done, or from purchase)

| Item | Interval | Code id |
| --- | --- | --- |
| Tyre rotation | Every 10,000 km (best range 5,000–7,000 km) | TIRE\_ROTATION |
| Throttle valve cleaning | Every 15,000 km | THROTTLE\_CLEAN |
| Four-wheel alignment check | Every 20,000 km | ALIGNMENT |
| Spark plugs (3707AAG) | Replace every 30,000 km | SPARK\_PLUGS |
| Brake fluid (DOT4) | Replace every 2 years or 40,000 km | BRAKE\_FLUID |
| Coolant (engine and power battery loops) | Replace every 2 years or 40,000 km | COOLANT |
| Transmission fluid | Replace every 2 years or 40,000 km | TRANSMISSION\_FLUID |
| Fuel filter (internal) | Replace every 80,000 km | FUEL\_FILTER |
| Charcoal canister filter (if fitted) | Replace every 3 years or 60,000 km | CHARCOAL\_CANISTER |
| Fuel filler hose (rubber NBR/PVC/CSM) | Replace every 5 years or 200,000 km | FUEL\_FILLER\_HOSE |
| Fuel vapour rubber pipe | Replace every 10 years or 500,000 km | FUEL\_VAPOR\_PIPE |
| Timing chain, 12V battery | Maintenance-free | — (show as info only) |

**First service checklist (p. 226, first 5,000 km or 3 months):** 25 checks — lubrication/cooling/fuel leaks; replace engine oil and filter; transmission fluid level and colour; coolant level and concentration; transmission leaks; steering tie-rod boots; drive shaft boots; brake fluid leaks and pipes; brake wheel cylinders; tyre pressures; alternator and A/C belt tension; hood hinge and latch; A/C leaks; steering universal joint boot; smooth gear shifting; chassis-to-body bolts; seat belts; engine idle, injection and emissions; road test; power battery surface; power battery fixing bolts; power battery vent valve; power battery connector; low-voltage harness connectors; high-voltage connectors and harness.

**Fluids and parts (p. 234)**

| Item | Specification | Capacity |
| --- | --- | --- |
| Engine oil | C3 0W-30 | 4.5 ± 0.2 L (with filter) |
| Transmission fluid | TITAN EG DHT 5105 (schedule page says "130HHB") | 4.1 ± 0.1 L |
| Engine coolant (high temp) | OAT | 7.1 ± 0.5 L |
| Power battery coolant (low temp) | OAT | 3.5 ± 0.5 L |
| Brake fluid | DOT4 | Between MIN and MAX |
| Spark plug | 3707AAG | — |

**Severe use (p. 233):** high humidity, mountains, extreme heat or cold, rough roads, frequent short trips, heavy urban traffic above 32 °C, or commercial use mean items should be replaced more often. The app shows this as a note; it does not change the intervals.

## Warranty (warranty booklet pp. 4–6)

Every warranty runs from the sales-invoice date and ends at its month limit or km limit, whichever comes first. Km are read from the instrument cluster; if the cluster is damaged, OMODA & JAECOO count 3,000 km per month.

**Main coverage**

| Coverage | Months | Km |
| --- | --- | --- |
| Whole vehicle | 72 | 150,000 |
| Power battery cells | 96 | 160,000 |
| Paint and surface corrosion (not perforation) | 24 | 50,000 |

**Special parts (shorter limits)**

| Part | Months | Km |
| --- | --- | --- |
| Tyres | 6 | 10,000 |
| 12V battery | 12 | 30,000 |
| Remote key battery | 12 | 30,000 |
| Exhaust system | 24 | 60,000 |
| Timing belt | 36 | 60,000 |
| Fuel injector | 48 | 100,000 |
| Fuel pump | 36 | 60,000 |
| Oil pump | 36 | 60,000 |
| Water pump | 36 | 60,000 |
| Brake disc | 36 | 60,000 |
| Brake caliper | 36 | 60,000 |
| CD/DVD | 36 | 60,000 |
| Starter | 36 | 60,000 |
| Alternator | 36 | 60,000 |
| Hub bearing | 36 | 60,000 |
| Window glass regulator | 36 | 60,000 |
| A/C compressor | 48 | 100,000 |
| Shock absorber | 36 | 60,000 |

**Spare parts fitted later:** genuine parts bought and installed at an authorized dealer get 12 months or 20,000 km from the install date. Exceptions with 3 months or 5,000 km: glass (including mirrors), wiper blades, clutch plate, brake disc/drum, brake pads/shoes. The app should start these warranties from the date of a REPAIR or PERIODIC record when the owner ticks "genuine part fitted" (Phase 4).

**Not covered:** bulbs, fuses, lubricating oil, air filter, oil filter, fuel filter, A/C filter, spark plugs and coolant.

**Conditions to show in the app:** use only genuine parts and fluids, use only brands and specs recommended by the authorized service station, and get scheduled services done at an authorized OMODA & JAECOO dealer, or claim rights may be affected.

## Scheduling logic

Put all of this in a pure Kotlin `ScheduleEngine` with no Android imports, so it can be unit-tested. Every function takes `today: LocalDate` as a parameter instead of reading the clock.

1. **Driving rate.** `kmPerDay = latestKm / max(1, days(purchaseDate, latestReadingDate))`, at least 1. Optionally use only the last 90 days of readings once there are enough.
2. **Projection.** `dateForKm(k) = latestReadingDate + ceil((k − latestKm) / kmPerDay)` days (today if already passed). `kmAtDate(d) = latestKm + days(latestReadingDate, d) × kmPerDay`.
3. **Next periodic service.** Let `n` = number of PERIODIC records + 1. Base = the last PERIODIC record's date and km, or the purchase date and 0 km. `dueDate = base.date + 3 months`, `dueKm = base.km + 5,000`. Effective due date = the earlier of `dueDate` and `dateForKm(dueKm)`. Cycle position = `((n − 1) mod 10) + 1` → look up air and A/C filter codes.
4. **Upcoming list.** For i = 1…8, project service `n + i − 1` at `base.date + 3i months` and `base.km + 5,000i`, same "earlier of" rule.
5. **Distance/time items.** For each `IntervalRule`, last done = the latest record whose `itemsDone` contains it, else purchase. `dueKm = last.km + rule.km`; `dueDate = last.date + rule.months` (if any). Effective = the earlier of `dueDate` and `dateForKm(dueKm)`. Progress = max(km used ÷ km interval, days used ÷ days interval).
6. **Status.** OVERDUE if `today > dueDate` or `latestKm ≥ dueKm`; DUE\_SOON if within 14 days or 500 km; otherwise OK.
7. **Warranty.** `expiry = purchaseDate + months`; EXPIRED if `today ≥ expiry` or `latestKm ≥ km`; ENDING\_SOON within 60 days or 2,000 km; else ACTIVE.
8. **Month arithmetic.** Use `LocalDate.plusMonths`, which clamps to the month's last day (31 Jan + 1 month = 28/29 Feb).

**Unit tests to require (with the owner's real data):** purchase 2026-08-20, odometer 2,000 km on 2026-10-25 → rate ≈ 30.3 km/day; the 2026-09-21 visit is an INSPECTION, so next service = Service 1, due 2026-11-20 (date first, \~2,800 km expected); Service 2 due 2027-02-20; A/C filter R at Service 3 (2027-05-20); brake fluid, coolant and transmission fluid due 2028-08-20; tyre warranty expires 2027-02-20; whole-vehicle warranty 2032-08-20; battery cells 2034-08-20.

## Screens

Four tabs in a bottom navigation bar, plus a setup screen and two bottom sheets. Material 3 with dynamic colour and dark mode.

| Screen | What it shows | Actions |
| --- | --- | --- |
| Setup (first launch only) | Purchase date, current odometer | Save; prefilled with 20 Aug 2026 and 2,000 km for this car |
| **Home** | Odometer card; next service card (service #, effective due date, days left, km left, status chip, "date or km comes first"); checklist for that visit with R items highlighted (oil and filter, filters, any distance item due); first-service 25-point checklist when n = 1; severe-use note | Update odometer (sheet), Log this service (sheet) |
| **Schedule** | Next 8 periodic services with due date, due km, expected km, air and A/C filter codes; distance/time items with progress bars and expected dates | Tap an item to see its history |
| **History** | Service records newest first: date, km, type, items done, workshop, cost | Edit, delete (with undo snackbar), add |
| **Warranty** | Three main coverage cards with expiry date and progress; special-parts list with status chips; conditions and exclusions | — |
| Specs (from Warranty or a menu) | Fluids and capacities table; transmission fluid name note | — |

**Log service sheet:** date (default today), odometer (default latest), type, checkboxes for every `ItemId` (preticked: oil and filter for PERIODIC, any filter coded R, and distance items due within 1,000 km), workshop, cost in PKR, job card number, notes. Saving also adds an odometer reading if km is higher.

**Update odometer sheet:** a number field; reject values lower than the last reading with an inline error.

Keep the UI copy plain, use km and PKR, and format dates as "20 Nov 2026".

## Reminders

A `PeriodicWorkRequest` runs once a day (around 9:00 Pakistan time) and runs the engine against today's date. It posts at most one notification per item per stage, remembered in DataStore so the same alert is not repeated.

| Trigger | Notification |
| --- | --- |
| Service due in 7 days (setting: 3, 7 or 14) | "Service 1 due 20 Nov — oil and filter change, book the dealer" |
| Service due today or km limit reached | "Service 1 is due today" |
| Overdue | Repeat every 3 days until logged |
| Distance/time item due within 500 km or 14 days | "Brake fluid replacement due 20 Aug 2028" |
| No odometer update for 30 days | "Update your odometer so due dates stay accurate" |
| Warranty ending in 30 days | "Tyre warranty ends 20 Feb 2027" |

Request the `POST_NOTIFICATIONS` permission on Android 13+ during setup. Tapping a notification opens Home; the service notification has a "Log service" action that opens the log sheet.

## Build phases

Build in five phases and install on the phone after each one. Each phase has a prompt to paste into Claude Code.

1. **Engine and project skeleton.** *"Read PLAN.md. Create the Android project (Kotlin, Compose, Gradle KTS). Write `ScheduleRules.kt` with all data from the Maintenance schedule and Warranty sections, and `ScheduleEngine.kt` per Scheduling logic. Add JUnit tests for every case listed under Unit tests and make them pass. No UI yet beyond a placeholder screen."*
2. **Storage and Home screen.** *"Add Room with the three tables, a repository, the Setup screen and the Home screen with the update-odometer and log-service sheets."*
3. **Schedule, History, Warranty and Specs screens.** *"Add the bottom navigation and the remaining screens from the Screens table."*
4. **Reminders and spare-part warranties.** *"Add the WorkManager daily check and notifications from the Reminders section. Add a 'genuine part fitted' option to the log sheet that starts a 12-month / 20,000 km (or 3-month / 5,000 km for glass, wipers, brakes) spare-part warranty."*
5. **Backup and polish.** *"Add export and import of all data as a JSON file using the Storage Access Framework, an app icon, and dark mode checks."*

**Install on the phone:** connect it by USB, accept the debugging prompt, and press Run in Android Studio (or ask Claude Code to run `./gradlew installDebug`). For a file you can keep, ask for a signed release APK in Phase 5 and copy it to the phone.

**Check after each phase**

- [ ] Unit tests pass (`./gradlew test`)
- [ ] Home shows Service 1 due 20 Nov 2026 for the seed data
- [ ] Logging a service moves the next due date forward 3 months
- [ ] Data survives closing and reopening the app
- [ ] A test notification appears (Phase 4: temporarily set the worker to 15 minutes)
- [ ] Export, uninstall, reinstall, import restores everything (Phase 5)

## Seed data and decisions

Prefill the setup screen and offer to import this history on first launch:

| Record | Date | Km | Notes |
| --- | --- | --- | --- |
| Purchase (sales invoice) | 20 Aug 2026 | 0 | Start of all warranties |
| Free inspection (INSPECTION, not PERIODIC) | \~21 Sep 2026 | \~1,000 | 1 month / 1,000 km dealer inspection; date and km approximate |
| Odometer reading | 25 Oct 2026 | 2,000 | — |

**Decisions**

- The September dealer visit (21 Sep 2026, \~1,000 km) was an inspection only. Service 1 is next: the dealer booked it for 21 Nov 2026, while the manual's rule gives 20 Nov from a 20 Aug purchase. Use the sales-invoice date as the purchase date.
- Services are booked every 3 months by date. The owner drives well under 5,000 km a quarter, so the km limit is only a backstop; no interval setting is needed.
- Transmission fluid choice is left to the dealer; the app shows the spec-page name with the 130HHB note.
- The app stays fully offline, with JSON export as the backup.
