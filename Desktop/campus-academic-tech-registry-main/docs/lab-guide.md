# Lab Guide — Campus Academic Technology Registry

> **Duration:** ~45 minutes  
> **Audience:** Developers evaluating IBM Bob  
> **Prerequisites:** Java 25+, Maven 3, IBM Bob installed in your IDE

This lab walks you through two things:

1. **Running the application** — exploring the asset registry, maintenance tracker, and replacement recommendation engine.
2. **Running IBM Bob's Java Modernization workflow** — upgrading the project from Java 21 / Spring Boot 3.2.5 to Java 25 / Spring Boot 3.5.x with zero manual effort.

---

## Lab Overview

| Part | What You Will Do | Time |
|---|---|---|
| [Part 1 — Setup](#part-1--setup) | Clone, build, and run the app | ~5 min |
| [Part 2 — Application Walkthrough](#part-2--application-walkthrough) | Explore every feature of the registry | ~10 min |
| [Part 3 — Bob Java Modernization Workflow](#part-3--bob-java-modernization-workflow) | Run the automated upgrade from Java 21 → Java 25 | ~15 min |
| [Part 4 — Review the Results](#part-4--review-the-results) | Inspect every file Bob changed | ~10 min |
| [Part 5 — Optional: Manual Modernization Steps](#part-5--optional-manual-modernization-steps) | Apply the language-level improvements by hand | ~15 min |

---

## Part 1 — Setup

### Step 1.1 — Clone and build

```bash
git clone https://github.com/Alyssa-Mercado/campus-academic-tech-registry.git
cd campus-academic-tech-registry
mvn --batch-mode verify
```

Expected output:
```
BUILD SUCCESS
```

> The project ships with an in-memory H2 database. No database setup is required.

---

### Step 1.2 — Start the application

```bash
mvn spring-boot:run
```

The server starts in ~4 seconds. Open **http://localhost:8080** in your browser.

> The database is seeded automatically with **25 assets** and **35 maintenance events** on first start. It resets every time you restart.

---

### Step 1.3 — Confirm the baseline Java version

```bash
mvn help:evaluate -Dexpression=java.version -q -DforceStdout
```

Expected output: `21`

You will return to this step after the modernization workflow to confirm the upgrade.

---

## Part 2 — Application Walkthrough

### Step 2.1 — Dashboard (`http://localhost:8080/`)

The landing page shows a health summary of the entire asset registry.

Verify you can see:
- [ ] **Total assets** — should show `25`
- [ ] **By type** breakdown — Classroom PC, Projector, Smartboard, Camera, Microphone
- [ ] **Maintenance** tiles — upcoming and overdue counts
- [ ] **Warranty** tiles — expiring soon and expired counts
- [ ] **Replacement recommendations** count

Screenshot reference: [`docs/screenshots/dashboard.png`](screenshots/dashboard.png)

---

### Step 2.2 — Asset List (`http://localhost:8080/assets`)

1. Confirm **25 assets** are listed.
2. Use the **type filter** — select `Projector` → verify 5 results appear.
3. Use the **search box** — type `Science` → verify all Science Hall assets appear.
4. Clear the filter and return to the full list.

Screenshot reference: [`docs/screenshots/assets.png`](screenshots/assets.png)

---

### Step 2.3 — Asset Detail (`http://localhost:8080/assets/1`)

Open the first asset (Dell OptiPlex 7060). Verify:
- [ ] **Warranty badge** shows `Expired` in red — computed live from the expiry date, not stored
- [ ] **Age** is displayed in years — computed on the fly, never goes stale
- [ ] **Maintenance history** lists both past and upcoming events
- [ ] **Edit** and **Delete** action buttons are present

> Try the **Edit** button — update the serial number and save. Confirm you land back on the detail page with the change applied.

---

### Step 2.4 — Add a New Asset (`http://localhost:8080/assets/new`)

Fill in any values and click **Save**.

Verify:
- [ ] You are redirected to the new asset's detail page
- [ ] The asset count on the dashboard increments to `26`

---

### Step 2.5 — Maintenance Tracker (`http://localhost:8080/maintenance`)

1. **All** tab — confirm 35 events are listed, sorted by date.
2. **Upcoming** tab — events with future scheduled dates only.
3. **Overdue** tab — events flagged red because their date has passed.
4. Click **Mark Complete** on any overdue event — confirm it flips to `Completed` immediately.

> **Why this matters:** The overdue flag is computed lazily on `findAll()` — there is no scheduled job. The status transitions when you load the page.

---

### Step 2.6 — Replacement Recommendations (`http://localhost:8080/recommendations`)

Screenshot reference: [`docs/screenshots/recommendations.png`](screenshots/recommendations.png)

Review the flagged assets. Verify:
- [ ] **HIGH severity** (red badge) — assets where both rules fired
- [ ] **MEDIUM severity** (amber badge) — assets where only one rule fired

The two rules the engine applies:

| Rule | Condition |
|---|---|
| **Age** | Asset age exceeds the threshold for its type (PC: 5 yr, Projector: 7 yr, Smartboard: 8 yr, Camera: 6 yr, Microphone: 6 yr) |
| **Warranty + Maintenance** | Warranty is expired **and** at least one maintenance event is overdue |

> **HIGH** means both rules fired for the same asset. **MEDIUM** means only one applied.

---

## Part 3 — Bob Java Modernization Workflow

> **Before you start this part:** confirm the project is on the pre-modernization baseline — Java 21 / Spring Boot 3.2.5. If you have already run the workflow, see [Resetting the Baseline](#resetting-the-baseline) at the end of this guide.

### Step 3.1 — Trigger the workflow

In the IBM Bob chat panel inside your IDE, type:

```
Start the Java Modernization workflow
```

Bob launches the workflow and presents three sub-workflow options:

| Option | Purpose |
|---|---|
| **Java Upgrade** | Bump Java and Spring Boot versions, migrate javax → jakarta, fix CVEs |
| **Liberty Replatforming** | Migrate from WebSphere to Liberty (requires an AMA zip) |
| **UI Modernization** | Convert JSF or Struts UI into a React frontend |

Select **Java Upgrade**.

> From this point the workflow is fully automated. Bob does not ask you to review individual recipe changes or manually resolve errors.

---

### Step 3.2 — Stage 1: Initial Build (Baseline Check)

Bob compiles the project at its current state (Java 21 / Spring Boot 3.2.5) to establish a clean baseline before making any changes.

Expected output:
```
✅ Initial build passed — Java 21 / Spring Boot 3.2.5
   No errors. No warnings. Safe to proceed.
```

> If the baseline build fails, the workflow stops here and reports the pre-existing error. Fix it before re-triggering.

---

### Step 3.3 — Stage 2: OpenRewrite Recipes

Bob applies two OpenRewrite recipes automatically:

| Recipe | What it does |
|---|---|
| `UpgradeSpringBoot_3_5` | Bumps `spring-boot-starter-parent` `3.2.5` → `3.5.14`; sets `java.version` `21` → `25` in `pom.xml` |
| Jakarta namespace migration | Replaces `javax.persistence.*` with `jakarta.persistence.*` in `Asset.java` and `MaintenanceEvent.java` |

You do not need to do anything — watch the output as Bob applies the recipes.

---

### Step 3.4 — Stage 3: Build Error Detection and Fix

After the recipes run, Bob recompiles the project. For this codebase, **1 error in 1 file** is detected. Bob spawns a focused subagent to diagnose and resolve it automatically.

Expected output once the subagent completes:
```
✅ Build error resolved — 1 file fixed
   Compilation passing. Proceeding to vulnerability scan.
```

**Subagent cost:** ~$0.77

---

### Step 3.5 — Stage 4: Vulnerability Scan and Remediation

Bob resolves the full dependency tree (99 JARs) and scans every dependency against the [OSV vulnerability database](https://osv.dev).

**Result for this project:** 55 vulnerabilities across 8 dependency groups.

A second subagent patches all 55 by injecting BOM property overrides into `pom.xml`.

Expected output:
```
✅ Vulnerability remediation complete
   55 CVEs resolved. 0 remaining.
```

**Subagent cost:** ~$4.21

---

### Step 3.6 — Stage 5: Final Validation Build

Bob runs a final `mvn verify` to confirm the fully upgraded, fully patched project compiles and all tests pass.

Expected final output:
```
✅ Modernization Complete — Java 25 / Spring Boot 3.5.14

   BUILD SUCCESS
   Java version:          25 (Temurin)
   Spring Boot:           3.5.14
   JPA namespace:         jakarta.persistence.*
   Known vulnerabilities: 0 (was 55)
   Errors:                0
   Warnings:              0
```

**Subagent cost:** ~$0.13  
**Total workflow cost:** ~$5.11

---

## Part 4 — Review the Results

### Step 4.1 — Inspect `pom.xml`

Open [`pom.xml`](../pom.xml) and locate three areas:

**a) Parent version bump** (around line 6):
```xml
<parent>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.14</version>   <!-- was 3.2.5 -->
</parent>
```

**b) Java version property** (around line 14):
```xml
<properties>
    <java.version>25</java.version>   <!-- was 21 -->
```

**c) Vulnerability override block** (lines 15–23):
```xml
<!-- Explicit overrides for vulnerabilities not yet patched by Spring Boot 3.5.14 BOM -->
<logback.version>1.5.34</logback.version>
<jackson-bom.version>2.18.8</jackson-bom.version>
<tomcat.version>10.1.55</tomcat.version>
<spring-framework.version>6.2.19</spring-framework.version>
<spring-data-bom.version>2025.0.12</spring-data-bom.version>
<thymeleaf.version>3.1.5.RELEASE</thymeleaf.version>
<postgresql.version>42.7.12</postgresql.version>
<assertj.version>3.27.7</assertj.version>
```

> These are `<properties>` overrides — Spring Boot's BOM still controls the dependency graph. This is the recommended pattern for patching CVEs without forking the BOM.

---

### Step 4.2 — Inspect the Jakarta migration

Open [`Asset.java`](../src/main/java/com/university/assettracker/domain/Asset.java) and check the import at line 3:

```java
// After the workflow
import jakarta.persistence.*;
```

Open [`MaintenanceEvent.java`](../src/main/java/com/university/assettracker/domain/MaintenanceEvent.java) — same change applied.

> Spring Boot 3.0 moved from `javax` (Java EE) to `jakarta` (Jakarta EE 10). OpenRewrite applied this change uniformly across every entity in one automated pass.

---

### Step 4.3 — Inspect the CI pipeline

Open [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) and find the `setup-java` step:

```yaml
- name: Set up Java 21
  uses: actions/setup-java@v4
  with:
    java-version: '25'          # was '21'
    distribution: 'temurin'
    cache: maven
```

---

### Step 4.4 — Confirm application logic is untouched

Open the following files and confirm they are identical to their pre-upgrade state:

- [ ] [`ReplacementService.java`](../src/main/java/com/university/assettracker/service/ReplacementService.java) — business logic unchanged
- [ ] [`application.properties`](../src/main/resources/application.properties) — no configuration changes
- [ ] Any Thymeleaf template under `src/main/resources/templates/` — no template changes

> The workflow is scoped to the **platform layer only** — it never touches application logic.

---

### Step 4.5 — Run the upgraded app

```bash
mvn spring-boot:run
```

Open **http://localhost:8080** — the application runs identically to Part 2, now on Java 25 / Spring Boot 3.5.14.

---

## Part 5 — Optional: Manual Modernization Steps

The workflow handles the **required** platform changes. These additional improvements can be applied manually in Agent mode — they are language-level modernizations that are intentionally out of scope for an automated platform upgrade.

### Step 5.1 — Convert `ReplacementRecommendation` to a Java record

`ReplacementRecommendation` is a pure value object (no JPA mapping) — it can become a Java record. Ask Bob in Agent mode:

```
Convert ReplacementRecommendation.java to a Java record using a compact canonical constructor for the defensive copy.
```

Expected result — [`ReplacementRecommendation.java`](../src/main/java/com/university/assettracker/domain/ReplacementRecommendation.java):
```java
public record ReplacementRecommendation(Asset asset, List<String> reasons) {

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

> **Why not `Asset` and `MaintenanceEvent`?** JPA requires a public no-arg constructor and mutable setters for proxy generation. `@Entity` classes cannot be records. The hand-rolled Builder pattern they already have is the correct approach.

---

### Step 5.2 — Refactor `ReplacementService` to use `mapMulti`

Ask Bob in Agent mode:

```
Refactor ReplacementService.getRecommendations() to use Stream.mapMulti() instead of the forEach + ArrayList pattern.
```

This replaces the imperative `forEach` + mutable `ArrayList` approach with a functional `mapMulti` stream pipeline — the Java 16+ idiom for conditionally emitting zero or one element per input.

---

### Step 5.3 — Enable virtual threads

In [`application.properties`](../src/main/resources/application.properties), add:

```properties
# Java 25 / Spring Boot 3.2+ — virtual threads on Tomcat
spring.threads.virtual.enabled=true
```

Virtual threads (JEP 505, finalized in Java 25) allow Tomcat to handle each request on a lightweight virtual thread rather than a platform thread — improving throughput under concurrent load with no code changes required.

---

### Step 5.4 — Replace `Arrays.asList()` with `List.of()`

Ask Bob in Agent mode:

```
Replace Arrays.asList(AssetType.values()) and Arrays.asList(MaintenanceStatus...) with List.of() in AssetController and MaintenanceController, and remove the unused Arrays import.
```

`List.of()` (Java 9+) returns a truly immutable list and is the idiomatic modern alternative to `Arrays.asList()`.

---

## Before and After Summary

| Component | Before | After |
|---|---|---|
| Java version | 21 | **25** |
| Spring Boot | 3.2.5 | **3.5.14** |
| Spring Framework | 6.1.x | **6.2.19** |
| JPA namespace | `javax.persistence.*` | **`jakarta.persistence.*`** |
| Logback | BOM default | **1.5.34** (CVE-patched) |
| Jackson | BOM default | **2.18.8** (CVE-patched) |
| Tomcat | BOM default | **10.1.55** (CVE-patched) |
| Spring Data | BOM default | **2025.0.12** (CVE-patched) |
| PostgreSQL driver | BOM default | **42.7.12** (CVE-patched) |
| CI Java version | 21 | **25 (Temurin)** |
| Known vulnerabilities | 55 | **0** |

---

## Resetting the Baseline

If you want to re-run the workflow from scratch, reset to the pre-modernization commit:

```bash
git log --oneline
# Find the commit: "feat: add campus academic technology registry Spring Boot application"
git checkout <commit-hash>
```

Or stash current changes if the working tree is clean:

```bash
git stash
```

Then confirm the baseline builds on Java 21:

```bash
mvn --batch-mode verify
```

---

## Reference

| Document | What it covers |
|---|---|
| [`docs/demo-script.md`](demo-script.md) | Presenter-facing app walkthrough with talking points |
| [`docs/demo-script-java-modernization.md`](demo-script-java-modernization.md) | Presenter-facing Java Modernization demo script |
| [`docs/bob-java-modernization.md`](bob-java-modernization.md) | Technical deep-dive: every change the workflow made and why |
| [`docs/java25-upgrade-plan.md`](java25-upgrade-plan.md) | The 5-phase manual upgrade plan with file-level diffs |
| [`requests.http`](../requests.http) | HTTP request file for every endpoint (IntelliJ / VS Code REST Client) |
