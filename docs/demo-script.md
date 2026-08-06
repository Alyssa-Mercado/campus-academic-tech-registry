# Demo Script — Campus Academic Technology Registry

> **Audience:** Technical reviewers, stakeholders, or interviewers
> **Duration:** ~10 minutes (Part 1: 5 min · Part 2: 5 min)
> **Prerequisites:** Java 25+, Maven 3, browser open at `http://localhost:8080`

---

## Before You Start

```bash
git clone https://github.com/Alyssa-Mercado/campus-academic-tech-registry.git
cd campus-academic-tech-registry
mvn spring-boot:run
```

The app starts in ~4 seconds. 25 assets and 35 maintenance events are seeded automatically.
Open **http://localhost:8080** in your browser.

---

## Part 1 — Application Walkthrough

### 1.1 — Dashboard (`/`)

> _"The landing page gives you an at-a-glance health summary of every asset in the registry."_

Point out:
- **Total assets** counter (25)
- **By type** breakdown — Classroom PC, Projector, Smartboard, Camera, Microphone
- **Maintenance** tiles: upcoming vs overdue
- **Warranty** tiles: expiring soon vs expired
- **Replacement recommendations** count — assets flagged by the rule engine

---

### 1.2 — Asset List (`/assets`)

> _"Every registered device is listed here. You can filter by type or search by name or building."_

Demo steps:
1. Show the full list — 25 assets
2. Filter by type — select `Projector` → 5 results
3. Search — type `Science` → all Science Hall assets

---

### 1.3 — Asset Detail (`/assets/1`)

> _"Each asset shows its full profile and maintenance history."_

On the Dell OptiPlex 7060, point out:
- **Warranty badge** — `Expired` (red), always computed live from the expiry date
- **Age** — always current, calculated on the fly
- **Maintenance history** — past and upcoming events listed together
- **Edit / Delete** actions

---

### 1.4 — Add a New Asset (`/assets/new`)

> _"Adding a new asset takes about 30 seconds."_

Fill in any values and click **Save** — you land directly on the new asset's detail page.

---

### 1.5 — Maintenance Tracker (`/maintenance`)

> _"Maintenance is what drives the replacement rule engine."_

Demo steps:
1. Show **All events** — 35 events sorted by date
2. Switch to **Upcoming** — pending workload for the next 30 days
3. Switch to **Overdue** — events flagged red automatically when their date passes
4. Click **Mark Complete** on one — it flips to `Completed` instantly

---

### 1.6 — Replacement Recommendations (`/recommendations`)

> _"The rule engine flags assets that need capital planning attention."_

Point out:
- **HIGH severity** (red) — both rules fired
- **MEDIUM severity** (amber) — one rule fired
- Two rules:
  - **Age** — asset exceeds the threshold for its type (e.g. PC: 5 yr, Projector: 7 yr)
  - **Warranty + Maintenance** — warranty expired and at least one maintenance event is overdue

---

## Part 2 — How IBM Bob Built This Application

> _"This entire project — from the initial requirements conversation through to the working application, the Java 25 upgrade plan, and this demo script — was built collaboratively with IBM Bob, an AI software engineer embedded directly in the IDE. Here's what that looked like at each stage of the SDLC."_

---

### 2.1 — Requirements & Architecture Design

**What Bob did:**
Before a single line of code was written, Bob was used to reason through the domain model interactively. Questions like _"what fields should an Asset have?", "how should warranty status work — stored or computed?", "what does a replacement rule engine look like?"_ were answered through a back-and-forth that produced the final architecture in minutes rather than hours.

**SDLC stage:** Requirements → Architecture

The design decisions that resulted — computed `@Transient` fields, thresholds externalised in `@ConfigurationProperties`, lazy overdue sync — were all reasoned through and justified before any implementation began.

**Example exchange:**
> _"Should WarrantyStatus be a stored column or computed at runtime?"_
> Bob: _"Computed — storing it creates a drift risk. A `@Transient` method on the entity derives it from `warrantyExpiryDate` every time, so it's always accurate with no sync job needed."_

---

### 2.2 — Application Build

**What Bob did:**
Bob generated the full application — all 14 source files — with production-quality code on the first pass:

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

A Spring Boot application of this scope typically takes 2–3 days to scaffold and wire correctly. With Bob, the full working application — seeded dataset, dual database profiles (H2 + PostgreSQL), and all Thymeleaf templates — was produced in a single session.

**Key quality decisions Bob enforced without being asked:**
- Constructor injection everywhere — no `@Autowired` field injection
- No JPQL written by hand — Spring Data derived query methods throughout
- No business logic in controllers — all routed to the service layer
- `application-dev.properties` for PostgreSQL kept separate from the default H2 profile
- Bootstrap 5 bundled locally — no CDN dependency, works offline

---

### 2.3 — Code Review & Reasoning

**What Bob did:**
At any point during development, Bob served as an always-available code reviewer with full project context — able to explain, critique, and justify specific design choices on demand.

**SDLC stage:** Code Review / Knowledge Transfer

**Example interactions:**

> _"Why does `syncAndSaveOverdue()` run on every `findAll()` call instead of using a `@Scheduled` job?"_
> Bob explained the tradeoff: a scheduled job needs a separate thread, a cron expression, and careful handling across H2 and PostgreSQL profiles. The lazy approach is simpler, has no timing gap, and is appropriate for a low-traffic admin tool.

> _"Is there a risk that `DataSeeder` runs twice?"_
> Bob pointed to the `if (assetRepository.count() != 0) return;` guard at the top of `run()` — idempotent by design.

> _"Should `ReplacementRecommendation` be an entity?"_
> Bob: _"No — it's a computed view over assets and maintenance events. Making it an entity would mean persisting derived data, which creates a consistency problem. It should stay a transient value object."_

Every architectural decision is documented, justified, and queryable — not locked in someone's head.

---

### 2.4 — Test Design & Seed Data

**What Bob did:**
Bob reasoned through which test scenarios the seed data needed to cover and designed the 25-asset / 35-event dataset to exercise every distinct code path:

| Scenario | Coverage |
|---|---|
| Rule 1 only (age > threshold) | Projector 8 yr old, Smartboard 9 yr old |
| Rule 2 only (expired warranty + overdue maintenance) | HP EliteDesk, Logitech Camera, Shure Microphone |
| Both rules → HIGH severity | Dell OptiPlex 7060 (6 yr, expired warranty, overdue event) |
| Warranty: Active | 8 assets with expiry > today + 90 days |
| Warranty: Expiring Soon | 6 assets with expiry within 90 days |
| Warranty: Expired | 12 assets with past expiry dates |
| Maintenance: Completed | 11 events |
| Maintenance: Scheduled (upcoming) | 10 future events |
| Maintenance: Overdue | 14 events with past scheduled dates |

Every dashboard counter, every filter view, and every recommendation badge has a specific asset backing it. Dates are relative (e.g. `today.minusYears(6)`) so the seed data never goes stale.

**SDLC stage:** Test Design / QA

---

### 2.5 — Documentation

**What Bob did:**
All project documentation was generated by Bob with full knowledge of the actual codebase — not generic boilerplate:

| Document | What it covers |
|---|---|
| [`README.md`](../README.md) | Tech stack, running locally, PostgreSQL dev profile, project structure, replacement rules |
| [`docs/java25-upgrade-plan.md`](java25-upgrade-plan.md) | 5-phase upgrade plan with file-level diffs, risk register, effort estimates |
| [`docs/demo-script.md`](demo-script.md) | This document — app walkthrough and Bob's SDLC contribution |
| [`requests.http`](../requests.http) | Full HTTP request file for every endpoint, compatible with IntelliJ and VS Code REST Client |

Every code reference links to the exact file. Every diff shows the actual before/after for this codebase. Documentation was produced alongside the code — not written last and left incomplete.

**SDLC stage:** Documentation

---

### 2.6 — Upgrade Planning & Modernization

**What Bob did:**
Given the completed codebase, Bob was asked: _"What would a Java 25 and Spring Boot 3.5 upgrade look like?"_ Rather than producing a generic migration guide, Bob read every source file, identified the exact constraints, and produced a project-specific upgrade plan with file-level diffs and per-change risk assessments. The full output is at [`docs/java25-upgrade-plan.md`](java25-upgrade-plan.md).

Bob's approach across three modes:

| Mode | Role | Applied here |
|---|---|---|
| **Ask** | Read-only exploration — cannot modify files | Analysed domain, service, and config files; confirmed Lombok unused; identified JPA record constraint |
| **Plan** | Produces a reviewable plan before any changes | Generated the 5-phase upgrade plan with risk register before touching a single file |
| **Agent** | Implements changes with a diff to Accept or Reject per file | Applied all 5 phases as isolated, reviewable changes; ran `mvn verify` to confirm |

**SDLC stage:** Technical Debt / Modernization Planning

A senior engineer would typically need to audit the full codebase before producing a credible upgrade plan. Bob did that audit in seconds and produced the plan with source-referenced justifications.

---

### 2.7 — The Full SDLC Picture

> _"Bob wasn't used as an autocomplete tool. It was used as a collaborative engineering partner across every stage of the development lifecycle."_

| SDLC Stage | Bob's Contribution | Without Bob |
|---|---|---|
| Requirements & Design | Reasoned through domain model tradeoffs in real-time | Whiteboard sessions, architecture docs written after the fact |
| Implementation | Generated production-quality code for all 14 source files in one session | 2–3 days of scaffolding and wiring |
| Code Review | Explained every design decision on demand with full project context | Synchronous senior reviewer required |
| Test Design | Designed the seed dataset to cover every code path and UI state | Manual QA planning, often incomplete |
| Documentation | Produced accurate, source-linked docs alongside the code | Written last, often skipped or generic |
| Modernization Planning | Produced a source-specific upgrade plan with file-level diffs and risk assessment | Senior engineer audit + separate documentation effort |

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
