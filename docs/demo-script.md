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

## Part 2 — Java 25 Modernization Walkthrough

> _"Now let's walk through what a Java 25 + Spring Boot 3.5 upgrade looks like for this exact codebase — and why it's low-risk."_

See the full plan at [`docs/java25-upgrade-plan.md`](java25-upgrade-plan.md).

---

### 2.1 — Toolchain Bump (Phase 1) — 5 minutes of work

> _"The single biggest change is two version numbers."_

**`pom.xml` — before:**
```xml
<parent>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.5</version>
</parent>
<properties>
    <java.version>21</java.version>
</properties>
```

**`pom.xml` — after:**
```xml
<parent>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.0</version>
</parent>
<properties>
    <java.version>25</java.version>
</properties>
```

Spring Boot 3.3 / 3.4 / 3.5 have **no breaking changes** in the JPA, Thymeleaf, or Web MVC layers this app uses. `mvn verify` passes green after the bump.

---

### 2.2 — Drop Lombok (Phase 2) — zero risk here

> _"Lombok is already declared `<optional>` in the POM, and there is not a single `@Data`, `@Builder`, or `@Getter` annotation anywhere in the source tree. The entities already have hand-rolled builders. Removing Lombok is a no-op deletion."_

Show the `pom.xml` Lombok block that gets removed — nothing in `src/` references it.

---

### 2.3 — `ReplacementRecommendation` → Java Record (Phase 2) — the most visible change

> _"This is the cleanest win. `ReplacementRecommendation` is a pure value object — it holds an asset and a list of reasons. It has no JPA annotations, no mutable state, no lifecycle callbacks. It's the textbook use case for a record."_

**Before** ([`ReplacementRecommendation.java`](../src/main/java/com/university/assettracker/domain/ReplacementRecommendation.java)):
```java
public class ReplacementRecommendation {
    private final Asset asset;
    private final List<String> reasons;

    public ReplacementRecommendation(Asset asset, List<String> reasons) {
        this.asset = asset;
        this.reasons = List.copyOf(reasons);
    }

    public Asset getAsset()          { return asset; }
    public List<String> getReasons() { return reasons; }
    // + getSeverity(), getSeverityBadgeClass()
}
```

**After:**
```java
public record ReplacementRecommendation(Asset asset, List<String> reasons) {

    // Compact canonical constructor — defensive copy
    public ReplacementRecommendation {
        reasons = List.copyOf(reasons);
    }

    public String getSeverity() {
        return reasons.size() >= 2 ? "HIGH" : "MEDIUM";
    }

    public String getSeverityBadgeClass() {
        return getSeverity().equals("HIGH") ? "bg-danger" : "bg-warning text-dark";
    }
}
```

**What we removed:** the explicit field declarations, the full constructor, and the two boilerplate accessors.  
**What the compiler generates for free:** `asset()`, `reasons()`, `equals()`, `hashCode()`, `toString()`.  
**Template impact:** zero — Thymeleaf 3.1 resolves both `asset()` and `getAsset()`.

> **Why the JPA entities can't be records:** `Asset` and `MaintenanceEvent` are `@Entity` classes. Hibernate's proxy mechanism requires a public no-arg constructor and mutable setters. Records are immutable and have no no-arg constructor — they stay as-is. This is not a limitation of this codebase; it's a fundamental JPA constraint.

---

### 2.4 — `ReplacementService` Stream Cleanup (Phase 2)

> _"The existing `getRecommendations()` method builds an `ArrayList`, mutates it inside a `forEach`, sorts it, then returns it. In Java 16+ we can use `Stream.mapMulti()` to express this as a single pipeline — no mutable intermediate list."_

**Before (key structure):**
```java
List<ReplacementRecommendation> result = new ArrayList<>();
assetRepository.findAll().forEach(asset -> {
    // ... build reasons list ...
    if (!reasons.isEmpty()) result.add(new ReplacementRecommendation(asset, reasons));
});
result.sort(...);
return result;
```

**After:**
```java
return assetRepository.findAll().stream()
    .<ReplacementRecommendation>mapMulti((asset, downstream) -> {
        // ... build reasons list ...
        if (!reasons.isEmpty()) downstream.accept(new ReplacementRecommendation(asset, reasons));
    })
    .sorted(...)
    .toList();   // returns unmodifiable list — already used elsewhere in the codebase
```

`mapMulti` is the right tool when a single input element should produce zero or one output element — exactly what this filter-and-wrap pattern is.

---

### 2.5 — Virtual Threads (Phase 3) — one line

> _"Spring Boot 3.2 added first-class virtual thread support for Tomcat. In Java 25 virtual threads are fully stable. Enabling them requires exactly one property — no code changes, no dependency changes."_

```properties
# application.properties
spring.threads.virtual.enabled=true
```

Every HTTP request is now dispatched on a virtual thread. For an I/O-bound application like this one (JPA queries on every request), this is a free throughput improvement — platform threads are no longer blocked waiting on JDBC.

---

### 2.6 — Language Features Already in Use

> _"One thing worth noting: this codebase already uses several post-Java-11 features correctly. The upgrade doesn't need to introduce them — they're already there."_

| Feature | JEP | Where in this codebase |
|---|---|---|
| Pattern matching `instanceof` | JEP 394 (Java 16) | `Asset.equals()`, `MaintenanceEvent.equals()` |
| Switch expressions | JEP 361 (Java 14) | `MaintenanceController.list()` — filter routing |
| `Stream.toList()` | Java 16 | `AssetService`, `MaintenanceService`, `ReplacementService` |
| `String.formatted()` | Java 15 | `ReplacementService` — reason message construction |
| `var` local inference | JEP 286 (Java 10) | `DashboardService.countByType()`, `ReplacementController.list()` |
| Records (after upgrade) | JEP 395 (Java 16) | `ReplacementRecommendation` |
| Virtual threads (after upgrade) | JEP 505 (Java 25) | All Tomcat request threads via `application.properties` |

---

### 2.7 — CI Pipeline (`/.github/workflows/ci.yml`)

> _"One line change in the workflow — swap Java 21 for 25."_

```yaml
# Before
- name: Set up Java 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'temurin'
    cache: maven

# After
- name: Set up Java 25
  uses: actions/setup-java@v4
  with:
    java-version: '25'        # use '25-ea' until GA ships (Sept 2025)
    distribution: 'temurin'
    cache: maven
```

---

### 2.8 — Risk Summary

> _"Three things make this upgrade particularly low-risk for this project:"_

1. **No annotation processors** — no kapt, no Lombok APT, no MapStruct. Annotation processing is the most common source of Java version friction.
2. **No reflection-heavy libraries** — no Jackson (JSON), no custom serialisation. Hibernate's internal reflection is handled by Boot's auto-configuration.
3. **Already modern** — the codebase doesn't use any deprecated APIs from Java 9–21. There's nothing to un-migrate.

Total lines changed across all five phases: **~30 lines** across 6 files.

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
