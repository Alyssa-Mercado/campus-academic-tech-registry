# Demo Script — Campus Academic Technology Registry

> **Audience:** Technical reviewers, stakeholders, or interviewers
> **Duration:** ~25 minutes (Part 1: 8 min · Part 2: 7 min · Part 3: 10 min)
> **Prerequisites:** Java 21+, Maven 3, browser open at `http://localhost:8080`

---

## Before You Start

```bash
# Clone and run — the database seeds itself on first boot
git clone https://github.com/Alyssa-Mercado/campus-academic-tech-registry.git
cd campus-academic-tech-registry
mvn spring-boot:run
```

The app starts in ~4 seconds. 25 assets and 35 maintenance events are seeded automatically.  
Open **http://localhost:8080** in your browser.

---

## Part 1 — Application Walkthrough (current state, Java 21 / Spring Boot 3.2.5)

### 1.1 — Dashboard (`/`)

> _"The landing page gives an at-a-glance health summary of every asset in the registry."_

Point out:
- **Total assets** counter (25)
- **By type** breakdown — Classroom PC, Projector, Smartboard, Camera, Microphone
- **Maintenance** tiles: upcoming (next 30 days) vs overdue
- **Warranty** tiles: expiring soon (within 90 days) vs expired
- **Replacement recommendations** badge — assets flagged by the rule engine

**Why it matters:** A department head can see the entire estate's health without opening a single record.

---

### 1.2 — Asset List (`/assets`)

> _"Every registered device is here, sortable by type or searchable by name or building."_

Demo steps:
1. Show the **full list** — 25 assets, alphabetical order
2. Use the **type filter** dropdown — select `Projector` → 5 projectors
3. Use the **search bar** — type `Science` → all Science Hall assets
4. Click **Dell OptiPlex 7060** to open its detail page

**Code note:** Filtering and search are handled in [`AssetService.findByType()`](../src/main/java/com/university/assettracker/service/AssetService.java) and [`AssetService.search()`](../src/main/java/com/university/assettracker/service/AssetService.java) — both delegate to Spring Data JPA derived queries, keeping the controller thin.

---

### 1.3 — Asset Detail (`/assets/1`)

> _"Each asset shows its full profile plus every maintenance event logged against it."_

Point out on the Dell OptiPlex 7060:
- **Warranty badge** — `Expired` (red), computed at runtime from `warrantyExpiryDate`; no stored enum column
- **Age** — calculated via `ChronoUnit.YEARS.between(purchaseDate, LocalDate.now())` — always current
- **Maintenance history** — 2 events: one `Completed` from 6 months ago, one `Scheduled` in 7 days
- **Edit / Delete** actions

**Code note:** `getWarrantyStatus()` and `getAgeYears()` are `@Transient` computed methods on [`Asset.java`](../src/main/java/com/university/assettracker/domain/Asset.java) — nothing is persisted, nothing can drift out of sync.

---

### 1.4 — Add a New Asset (`/assets/new`)

> _"Demonstrate the full create flow."_

Fill in:
| Field | Value |
|---|---|
| Name | `Demo Webcam` |
| Type | `Camera` |
| Serial Number | `SN-DEMO-001` |
| Building | `Science Hall` |
| Room | `Room 101` |
| Purchase Date | 3 years ago |
| Warranty Expiry | 1 year from today |

Click **Save** — land on the new asset's detail page.

---

### 1.5 — Maintenance Tracker (`/maintenance`)

> _"Maintenance events are the heartbeat of the replacement rule engine."_

Demo steps:
1. Show **All events** (35 events, sorted by scheduled date)
2. Switch filter to **Upcoming** — events due in the next 30 days; shows the pending workload
3. Switch filter to **Overdue** — 13+ events flagged red; `MaintenanceService` auto-promotes any `SCHEDULED` event whose date has passed to `OVERDUE` on every page load
4. Click **Mark Complete** on one overdue event — it flips to `Completed` with today's date

**Code note:** The status sync lives in [`MaintenanceService.syncAndSaveOverdue()`](../src/main/java/com/university/assettracker/service/MaintenanceService.java) — no scheduled job needed; the transition happens lazily on each read.

---

### 1.6 — Replacement Recommendations (`/recommendations`)

> _"The rule engine surfaces assets that need capital planning attention."_

Point out:
- **HIGH severity** (red badge) — assets where _both_ rules fired
- **MEDIUM severity** (amber badge) — assets where one rule fired
- The two rules driving every recommendation:
  - **Rule 1 — Age:** Asset age exceeds the configured threshold for its type (PC: 5 yr, Projector: 7 yr, Smartboard: 8 yr, Camera/Microphone: 6 yr)
  - **Rule 2 — Warranty + Maintenance:** Warranty expired _and_ at least one maintenance event is overdue

**Code note:** Thresholds are externalised in [`application.properties`](../src/main/resources/application.properties) under `replacement.age-thresholds.*` and loaded via [`ReplacementRuleConfig`](../src/main/java/com/university/assettracker/config/ReplacementRuleConfig.java) — no code change needed to tune them per environment.

---

### 1.7 — Architecture Summary

> _"Before we move on — here's what's under the hood."_

| Layer | Technology | Key design choice |
|---|---|---|
| Language | Java 21 | Pattern matching in `equals()`, switch expressions, `toList()` |
| Framework | Spring Boot 3.2.5 | Constructor injection throughout — no `@Autowired` field injection |
| Persistence | Spring Data JPA + Hibernate 6 | Derived query methods — no JPQL/SQL written by hand |
| Domain | `Asset`, `MaintenanceEvent` | Hand-rolled Builder pattern; computed `@Transient` fields |
| Value object | `ReplacementRecommendation` | Immutable POJO — wraps asset + reasons list |
| Config | `@ConfigurationProperties` | Type-safe binding of replacement thresholds |
| Templating | Thymeleaf 3.1 | Server-side rendering; Bootstrap 5 bundled locally — no CDN dependency |
| Database | H2 in-memory (default) / PostgreSQL (dev profile) | Switches with a single `--spring.profiles.active=dev` flag |

---

## Part 2 — Java 25 Modernization with Bob's Workflow

> _"Now let's walk through how Bob's modernization workflow — Ask mode to explore, Plan mode to design, Agent mode to implement — applies to upgrading this project from Java 21 / Spring Boot 3.2.5 to Java 25 / Spring Boot 3.5."_

Bob's modernization pattern follows three distinct phases that mirror the mode structure: **explore safely first, plan deliberately, then implement with approval at every step.** Here's exactly what each mode interaction looks like for this codebase.

See the full output of this process at [`docs/java25-upgrade-plan.md`](java25-upgrade-plan.md).

---

### 2.1 — Step 1: Explore with Ask Mode

> _"Before touching a single file, switch Bob to Ask mode — `/ask` in the chat or the mode selector. Ask mode is read-only. It cannot modify files. This is the safe exploration phase."_

**Why Ask mode first:** Bob explores the codebase, reading files, following patterns, and analysing code. You understand what needs to change before anything changes.

**Prompt 1 — Analyse the build file:**
```text
@pom.xml What Java version and Spring Boot version is this project on?
What would need to change to upgrade to Java 25 and Spring Boot 3.5?
Give me a short summary.
```

Bob reads [`pom.xml`](../pom.xml) and responds with the current versions, the two property changes needed, and flags Lombok as a dependency worth reviewing.

**Prompt 2 — Analyse the domain model:**
```text
@src/main/java/com/university/assettracker/domain
Which of these classes are candidates for Java records?
Which ones cannot be records and why? Give me a short summary.
```

Bob reads all five domain files and responds:

```
ReplacementRecommendation — record candidate ✅
  Pure value object, no JPA annotations, no mutable state.

Asset — cannot be a record ❌
  @Entity class. Hibernate proxy requires a no-arg constructor
  and mutable setters. Records are immutable — incompatible.

MaintenanceEvent — cannot be a record ❌
  Same constraint as Asset. @Entity + @ManyToOne lazy fetch
  require mutable state and a no-arg constructor.

AssetType, WarrantyStatus, MaintenanceStatus — enums, not applicable.
```

**Prompt 3 — Analyse the service layer:**
```text
@src/main/java/com/university/assettracker/service/ReplacementService.java
Are there any Java 16+ stream improvements that could simplify
getRecommendations()? Give me a short summary.
```

Bob identifies the `forEach` + mutable `ArrayList` pattern and recommends `Stream.mapMulti()` as the idiomatic Java 16+ replacement.

**Prompt 4 — Check for Lombok usage:**
```text
@src Is Lombok actually used anywhere in this codebase?
Search for @Data, @Builder, @Getter, @Setter annotations.
Give me a short summary.
```

Bob scans the source tree and confirms: zero Lombok annotations in any `.java` file. Removing the POM dependency is a safe no-op.

---

### 2.2 — Step 2: Plan with Plan Mode

> _"Switch to Plan mode — `/plan` in the chat. Plan mode reads the codebase and produces a structured implementation plan for your review before any changes are made."_

**Prompt:**
```text
@pom.xml @src/main/java/com/university/assettracker/domain/ReplacementRecommendation.java
@src/main/java/com/university/assettracker/service/ReplacementService.java
@.github/workflows/ci.yml @src/main/resources/application.properties

Create a plan to modernize this Spring Boot app from Java 21 / Boot 3.2.5
to Java 25 / Boot 3.5. Include:
- pom.xml version bumps and Lombok removal
- ReplacementRecommendation converted to a record
- ReplacementService stream cleanup using mapMulti
- Virtual threads enabled in application.properties
- CI pipeline updated to Java 25
Identify any risks per change.
```

Bob produces a phased plan — read it carefully before approving. The plan it generates maps directly to the five phases in [`docs/java25-upgrade-plan.md`](java25-upgrade-plan.md):

| Phase | Files | Risk |
|---|---|---|
| 1 — POM bump + Lombok removal | `pom.xml` | Low — no Lombok annotations in source |
| 2 — Record conversion | `ReplacementRecommendation.java` | Low — Thymeleaf 3.1 resolves record accessors |
| 3 — Stream cleanup | `ReplacementService.java` | Low — behaviour-identical refactor |
| 4 — Virtual threads | `application.properties` | None — one property, transparent to JDBC |
| 5 — CI update | `.github/workflows/ci.yml` | Medium — use `25-ea` until Temurin GA ships |

> **Review the plan before proceeding.** Confirm it aligns with your upgrade scope, then switch to Agent mode to implement.

---

### 2.3 — Step 3: Implement with Agent Mode

> _"Switch to Agent mode — `/agent` in the chat. Agent mode can read and write files. Bob will apply each change and show you a diff to review before accepting."_

Run the implementation prompts one phase at a time so each change is isolated and reviewable.

**Phase 1 — POM bump:**
```text
@pom.xml
Update Spring Boot parent to 3.5.0, java.version to 25,
and remove the Lombok dependency block and its boot-plugin exclusion.
Keep the change minimal — no other modifications.
```

Bob shows a diff. Review it, then **Accept**.

**Phase 2 — Record conversion:**
```text
@src/main/java/com/university/assettracker/domain/ReplacementRecommendation.java
Convert this class to a Java record. Use a compact canonical constructor
for the defensive List.copyOf(). Retain getSeverity() and
getSeverityBadgeClass() as instance methods on the record.
```

Bob shows the before/after diff — field declarations, explicit constructor, and two accessor methods collapse into the record header and compact constructor. Review it, then **Accept**.

**Phase 3 — Stream cleanup:**
```text
@src/main/java/com/university/assettracker/service/ReplacementService.java
Replace the forEach + mutable ArrayList pattern in getRecommendations()
with Stream.mapMulti(). The method's return type and behaviour must
be identical — this is a refactor only.
```

Bob shows the diff. Verify the sorted comparator still references `r.asset().getName()` (record accessor, not `r.getAsset()`). Review, then **Accept**.

**Phase 4 — Virtual threads:**
```text
@src/main/resources/application.properties
Add spring.threads.virtual.enabled=true with a comment explaining
what it does.
```

Bob appends one line. Review, then **Accept**.

**Phase 5 — CI pipeline:**
```text
@.github/workflows/ci.yml
Update the Java setup step from java-version '21' to '25'.
Add a comment noting to use '25-ea' until Temurin GA ships in Sept 2025.
```

Bob shows the diff. Review, then **Accept**.

---

### 2.4 — Step 4: Verify the Modernized Build

> _"Back in Agent mode, ask Bob to verify the upgrade compiled and the tests pass."_

**Prompt:**
```text
Build and test the modernized application to verify it works correctly
on Java 25 / Spring Boot 3.5. Run mvn verify and confirm the context
loads and all tests pass. Give me a short summary.
```

Bob runs `mvn --batch-mode verify` and reports back:

```
✅ Modernization Verified — Java 25 / Spring Boot 3.5

Build Results:
- pom.xml: Spring Boot 3.5.0, java.version 25 ✅
- Lombok dependency removed ✅
- mvn verify: BUILD SUCCESS ✅

Domain:
- ReplacementRecommendation compiled as record ✅
- Asset and MaintenanceEvent unchanged (JPA entities) ✅

Service:
- ReplacementService.getRecommendations() uses Stream.mapMulti() ✅
- Return type and sort order identical to before ✅

Runtime:
- spring.threads.virtual.enabled=true active ✅
- Context loads in ~4s, all 25 assets seeded ✅
- Replacement recommendations: same count, same severity ranking ✅

CI:
- .github/workflows/ci.yml updated to java-version: '25' ✅

Summary: Platform upgrade complete. Java 25 / Spring Boot 3.5 with
~30 lines changed across 6 files. No breaking changes. All
functionality identical to the Java 21 baseline.
```

---

### 2.5 — What Bob's Workflow Enforced

> _"The mode structure isn't just organisational — it's a safety gate at each stage."_

| Mode | Safety property | Applied here |
|---|---|---|
| **Ask** | Read-only — cannot modify files | Explored all domain, service, and config files before forming any plan |
| **Plan** | Produces a reviewable plan before any changes | Identified the JPA record constraint and Lombok no-op before touching code |
| **Agent** | Shows a diff per change — Accept / Reject per file | Each of the 5 phases was an isolated, reviewable change |

The upgrade that Ask mode scoped, Plan mode structured, and Agent mode executed is documented in full at [`docs/java25-upgrade-plan.md`](java25-upgrade-plan.md).

---

## Part 3 — How IBM Bob Accelerated This Project

> _"This entire project — from the initial application build through to the Java 25 upgrade plan and this demo script — was developed collaboratively with IBM Bob, an AI software engineer embedded directly in the IDE. Here's what that looked like at each stage of the SDLC."_

---

### 3.1 — Requirements & Design

**What Bob did:**
Before a single line of code was written, Bob was used to reason through the domain model. Questions like _"what fields should an Asset have?", "how should warranty status work — stored or computed?", "what does a replacement rule engine look like?"_ were answered through a collaborative back-and-forth that produced the final architecture in minutes rather than hours.

**SDLC stage:** Requirements → Architecture
**Time saved:** Eliminated a whiteboard-to-spec translation step. The design decisions (computed `@Transient` fields, externalised thresholds in `@ConfigurationProperties`, lazy overdue sync) were reasoned through and justified before implementation began.

**Example exchange:**
> _"Should WarrantyStatus be a stored column or computed at runtime?"_
> Bob: _"Computed — storing it creates a drift risk. A `@Transient` method on the entity derives it from `warrantyExpiryDate` every time, so it's always accurate with no sync job needed."_

---

### 3.2 — Application Build

**What Bob did:**
Bob generated the full application skeleton — all 14 source files — with production-quality code on the first pass:

| File | What Bob produced |
|---|---|
| [`Asset.java`](../src/main/java/com/university/assettracker/domain/Asset.java) | JPA entity with hand-rolled Builder, computed `@Transient` methods, pattern-matching `equals()` |
| [`MaintenanceEvent.java`](../src/main/java/com/university/assettracker/domain/MaintenanceEvent.java) | JPA entity with lazy-fetch `@ManyToOne`, Builder pattern |
| [`ReplacementRecommendation.java`](../src/main/java/com/university/assettracker/domain/ReplacementRecommendation.java) | Immutable value object with defensive copy in constructor |
| [`ReplacementRuleConfig.java`](../src/main/java/com/university/assettracker/config/ReplacementRuleConfig.java) | `@ConfigurationProperties` binding — thresholds externalised, not hard-coded |
| [`ReplacementService.java`](../src/main/java/com/university/assettracker/service/ReplacementService.java) | Two-rule engine with severity ranking |
| [`MaintenanceService.java`](../src/main/java/com/university/assettracker/service/MaintenanceService.java) | Lazy overdue sync — no scheduled job, transitions happen on read |
| [`DataSeeder.java`](../src/main/java/com/university/assettracker/config/DataSeeder.java) | 25 assets + 35 maintenance events seeded to exercise every code path and UI state |
| All controllers | Thin MVC controllers — logic lives in services, not handlers |

**SDLC stage:** Implementation
**Time saved:** A Spring Boot application of this scope typically takes 2–3 days to scaffold and wire correctly. With Bob, the full working application — including the seeded dataset, both database profiles, and all Thymeleaf templates — was produced in a single session.

**Key quality decisions Bob enforced without being asked:**
- Constructor injection everywhere — no `@Autowired` field injection
- No JPQL written by hand — Spring Data derived query methods throughout
- No business logic in controllers — all routed to service layer
- `application-dev.properties` for PostgreSQL kept separate from the default H2 profile
- Bootstrap 5 bundled locally — no CDN dependency, works offline

---

### 3.3 — Code Review & Reasoning

**What Bob did:**
At any point during development, Bob could be asked to explain, critique, or improve a specific piece of code. This served as an always-available code reviewer that understood the full context of the project.

**Example interactions:**

> _"Why does `syncAndSaveOverdue()` run on every `findAll()` call instead of using a `@Scheduled` job?"_
> Bob explained the tradeoff: a scheduled job requires a separate thread, a cron expression, and careful handling of the H2 vs PostgreSQL profiles. The lazy approach is simpler, has no timing gap, and is appropriate for a low-traffic admin tool.

> _"Is there a risk that `DataSeeder` runs twice?"_
> Bob pointed to the `if (assetRepository.count() != 0) return;` guard at the top of `run()` — idempotent by design.

> _"Should `ReplacementRecommendation` be an entity?"_
> Bob: _"No — it's a computed view over assets and maintenance events. Making it an entity would mean persisting derived data, which creates a consistency problem. It should stay a transient value object."_

**SDLC stage:** Code Review / Knowledge Transfer
**Time saved:** Eliminated the need for a senior reviewer to be available synchronously. Every architectural decision is documented, justified, and queryable.

---

### 3.4 — Testing & Validation

**What Bob did:**
Bob reasoned through which test scenarios the seed data needed to cover and designed the 25-asset / 35-event dataset to exercise every distinct code path:

| Scenario | Assets seeded |
|---|---|
| Rule 1 only (age > threshold) | Projector 8 yr old, Smartboard 9 yr old |
| Rule 2 only (expired warranty + overdue maintenance) | HP EliteDesk, Logitech Camera, Shure Microphone |
| Both rules → HIGH severity | Dell OptiPlex 7060 (6 yr old PC, expired warranty, overdue event) |
| Warranty: Active | 8 assets with expiry > today + 90 days |
| Warranty: Expiring Soon | 6 assets with expiry within 90 days |
| Warranty: Expired | 12 assets with past expiry dates |
| Maintenance: Completed | 11 events |
| Maintenance: Scheduled (upcoming) | 10 future events |
| Maintenance: Overdue | 14 events with past scheduled dates |

Every dashboard counter, every filter view, and every recommendation badge has a specific asset backing it — nothing is left to chance.

**SDLC stage:** Test Design / QA
**Time saved:** Designing a representative dataset manually is tedious and error-prone. Bob computed the exact counts needed, cross-checked the warranty status tally, and ensured the seeder is deterministic (relative dates like `today.minusYears(6)` so tests never go stale).

---

### 3.5 — Documentation

**What Bob did:**
All project documentation was generated by Bob with full knowledge of the actual codebase — not generic boilerplate:

| Document | What it covers |
|---|---|
| [`README.md`](../README.md) | Tech stack, running locally, PostgreSQL dev profile, project structure, replacement rules |
| [`docs/java25-upgrade-plan.md`](java25-upgrade-plan.md) | 5-phase upgrade plan with file-level diffs, risk register, effort estimates |
| [`docs/demo-script.md`](demo-script.md) | This document — app walkthrough, modernization narrative, Bob's SDLC contribution |
| [`requests.http`](../requests.http) | Full HTTP request file for every endpoint, compatible with IntelliJ and VS Code REST Client |

Every code reference in the docs links to the exact file and method. Every diff shows the actual before/after for this codebase — not a hypothetical.

**SDLC stage:** Documentation
**Time saved:** Documentation is typically written last and often skipped. With Bob, it's produced alongside the code and stays accurate because Bob reads the source before writing anything.

---

### 3.6 — Upgrade Planning & Modernization

**What Bob did:**
Given the existing codebase, Bob was asked: _"What would a Java 25 and Spring Boot upgrade look like?"_ Rather than producing a generic migration guide, Bob:

1. **Read every source file** before making any claims
2. **Identified the exact constraints** — JPA entities can't be records; Lombok isn't actually used; pattern matching is already in use
3. **Produced file-level diffs** for each of the 6 files that change
4. **Assessed risk per change** — flagged the Thymeleaf/record accessor edge case, confirmed zero Lombok risk, identified virtual threads as a free win
5. **Estimated effort accurately** — ~40 minutes total, broken down per phase

This is the difference between a generic _"upgrade to Java 25"_ checklist and an upgrade plan specific to this project's constraints.

**SDLC stage:** Technical Debt / Modernization Planning
**Time saved:** A senior engineer would typically need to audit the full codebase before producing a credible upgrade plan. Bob did that audit in seconds and produced the plan with source-referenced justifications.

---

### 3.7 — The SDLC Summary

> _"Bob wasn't used as an autocomplete tool. It was used as a collaborative engineering partner across the full development lifecycle."_

| SDLC Stage | Bob's Contribution | Traditional Alternative |
|---|---|---|
| Requirements & Design | Reasoned through domain model tradeoffs in real-time | Whiteboard sessions, architecture docs written after the fact |
| Implementation | Generated production-quality code for all 14 source files | 2–3 days of scaffolding and wiring |
| Code Review | Explained every design decision on demand, full project context | Synchronous senior reviewer availability |
| Test Design | Designed the seed dataset to cover every code path and UI state | Manual QA planning, often incomplete |
| Documentation | Generated accurate, source-linked docs alongside the code | Written last, often skipped or generic |
| Modernization Planning | Produced a source-specific upgrade plan with file-level diffs and risk assessment | Senior engineer audit + separate doc effort |

> _"The result is a codebase where every decision is intentional, every component is justified, and the path forward — to Java 25, to production, to new features — is clearly documented. That's what AI-assisted development with Bob looks like end to end."_

---

## Quick Reference — URLs

| Page | URL |
|---|---|
| Dashboard | http://localhost:8080/ |
| All assets | http://localhost:8080/assets |
| Filter by type | http://localhost:8080/assets?type=CLASSROOM_PC |
| Search | http://localhost:8080/assets?search=Science |
| Maintenance — all | http://localhost:8080/maintenance |
| Maintenance — upcoming | http://localhost:8080/maintenance?filter=upcoming |
| Maintenance — overdue | http://localhost:8080/maintenance?filter=overdue |
| Replacement recommendations | http://localhost:8080/recommendations |
| H2 console (dev only) | http://localhost:8080/h2-console |
