# Demo Script — Campus Academic Technology Registry

> **Audience:** Technical reviewers, stakeholders, or interviewers  
> **Duration:** ~15 minutes (Part 1: 8 min · Part 2: 7 min)  
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
