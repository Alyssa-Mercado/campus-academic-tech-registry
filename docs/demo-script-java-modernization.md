# Demo Script — Java Modernization with IBM Bob

> **Audience:** Technical reviewers, stakeholders, or interviewers
> **Duration:** ~15 minutes
> **Prerequisites:** IBM Bob installed in your IDE, project open at `classroom-asset-tracker/`

---

## Before You Start

Make sure the project is on the **pre-modernization baseline** (Java 21 / Spring Boot 3.2.5) so the
workflow has something to upgrade. If you are already on Java 25 / Spring Boot 3.5.14, the workflow
will find no version changes to apply and the demo will not be representative.

To reset to the baseline:

```bash
git stash        # or git checkout the pre-workflow commit
mvn --batch-mode verify   # confirm it builds clean on Java 21
```

---

## Part 1 — Trigger the Workflow (~2 min)

> _"Bob ships a built-in Java Modernization workflow. Instead of manually editing POM files,
> running OpenRewrite recipes, and hunting for CVEs, you trigger one workflow and Bob handles
> the entire platform upgrade."_

### 1.1 — Open the Java Modernization Workflow

In the Bob chat panel, type:

```text
Start the Java Modernization workflow
```

Bob launches the workflow and displays the three available sub-workflow options:

| Option | When to use |
|---|---|
| **Java Upgrade** | Bump the Java version and Spring Boot version, migrate javax → jakarta, fix CVEs |
| **Liberty Replatforming** | Migrate from WebSphere to Liberty (requires an AMA zip file) |
| **UI Modernization** | Convert JSF or Struts UI into a React frontend + Java backend |

### 1.2 — Select Java Upgrade

Select **Java Upgrade**. Bob immediately begins the workflow — no further input required.

> **Key point:** The workflow is fully automated from here. Bob does not ask you to review
> individual recipe changes or manually fix errors. Each stage runs, and subagents are spawned
> where focused work is needed.

---

## Part 2 — Watch the Workflow Execute (~8 min)

> _"Let's walk through each stage Bob runs through automatically."_

### 2.1 — Stage 1: Initial Build (Baseline Check)

Bob compiles the project at its current state — Java 21 / Spring Boot 3.2.5 — and confirms
a clean build before making any changes.

**Why it matters:** Bob establishes a baseline so any issues introduced by the upgrade are
clearly attributable to the workflow, not pre-existing.

Expected output:
```
✅ Initial build passed — Java 21 / Spring Boot 3.2.5
   No errors. No warnings. Safe to proceed.
```

---

### 2.2 — Stage 2: OpenRewrite Recipes Applied

Bob applies two OpenRewrite recipes to the codebase automatically:

| Recipe | What it does to this project |
|---|---|
| `UpgradeSpringBoot_3_5` | Bumps `spring-boot-starter-parent` from `3.2.5` → `3.5.14`; sets `java.version` from `21` → `25` in [`pom.xml`](../pom.xml) |
| Jakarta namespace migration | Replaces `javax.persistence.*` with `jakarta.persistence.*` in [`Asset.java`](../src/main/java/com/university/assettracker/domain/Asset.java) and [`MaintenanceEvent.java`](../src/main/java/com/university/assettracker/domain/MaintenanceEvent.java) |

**Why the jakarta migration matters:** Spring Boot 3.0 moved from the `javax` namespace
(Java EE) to `jakarta` (Jakarta EE 10). Any project upgrading from Boot 2.x or early Boot 3.x
must update all JPA imports — OpenRewrite does this across every entity in one pass.

**Code note:** Open [`Asset.java`](../src/main/java/com/university/assettracker/domain/Asset.java)
line 3 to show the import before and after the recipe ran:

```java
// Before
import javax.persistence.*;

// After — applied by OpenRewrite
import jakarta.persistence.*;
```

---

### 2.3 — Stage 3: Build Error Detection and Fix (Subagent 1)

After the recipes run, Bob compiles the project again. For this codebase, **1 error in 1 file**
is detected. Bob spawns a focused subagent to diagnose and resolve it.

> _"This is one of the most valuable parts of the workflow. OpenRewrite handles the mechanical
> transformations, but not every project compiles cleanly after a major version bump. Bob
> automatically detects what broke and fixes it — you don't need to read a stack trace."_

Expected output once the subagent completes:
```
✅ Build error resolved — 1 file fixed
   Compilation passing. Proceeding to vulnerability scan.
```

**Subagent cost:** ~$0.77

---

### 2.4 — Stage 4: Vulnerability Scan and Remediation (Subagent 2)

Bob resolves the dependency tree (99 dependencies for this project) and scans every JAR
against the [OSV vulnerability database](https://osv.dev).

**Result for this project:** 55 vulnerabilities found across 8 dependency groups.

A second subagent remediates all 55 by injecting **BOM property overrides** into
[`pom.xml`](../pom.xml):

```xml
<logback.version>1.5.34</logback.version>
<jackson-bom.version>2.18.8</jackson-bom.version>
<tomcat.version>10.1.55</tomcat.version>
<spring-framework.version>6.2.19</spring-framework.version>
<spring-data-bom.version>2025.0.12</spring-data-bom.version>
<thymeleaf.version>3.1.5.RELEASE</thymeleaf.version>
<postgresql.version>42.7.12</postgresql.version>
<assertj.version>3.27.7</assertj.version>
```

**Code note:** Open [`pom.xml`](../pom.xml) lines 23–31 to show the override block. Point out:
- These are `<properties>` entries, not dependency replacements
- Spring Boot's BOM still controls the dependency graph; these just pin the security-critical
  versions above what the BOM currently specifies
- This is the Spring Boot-recommended approach for vulnerability remediation

> _"55 CVEs resolved by one subagent in a single pass. Doing this manually means reading each
> CVE advisory, finding the patched version, testing compatibility, and repeating 55 times."_

**Subagent cost:** ~$4.21

---

### 2.5 — Stage 5: Final Validation Build (Subagent 3)

Bob runs one final `mvn verify` to confirm the fully upgraded, fully patched project compiles
and all tests pass.

Expected output:
```
✅ Modernization Complete — Java 25 / Spring Boot 3.5.14

   BUILD SUCCESS
   Java version:         25 (Temurin)
   Spring Boot:          3.5.14
   JPA namespace:        jakarta.persistence.*
   Known vulnerabilities: 0 (was 55)
   Errors:               0
   Warnings:             0
```

**Subagent cost:** ~$0.13

---

## Part 3 — Show the Results (~5 min)

> _"Let's look at exactly what changed."_

### 3.1 — pom.xml

Open [`pom.xml`](../pom.xml) and point out three areas:

1. **Parent version** — `3.2.5` → `3.5.14`
2. **Java version property** — `21` → `25`
3. **Vulnerability override block** — 8 explicit version pins, all above the BOM defaults

```xml
<parent>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.14</version>   <!-- was 3.2.5 -->
</parent>

<properties>
    <java.version>25</java.version>   <!-- was 21 -->
    <!-- Explicit overrides for vulnerabilities not yet patched by Spring Boot 3.5.14 BOM -->
    <logback.version>1.5.34</logback.version>
    ...
</properties>
```

---

### 3.2 — Jakarta Imports

Open [`Asset.java`](../src/main/java/com/university/assettracker/domain/Asset.java) and
[`MaintenanceEvent.java`](../src/main/java/com/university/assettracker/domain/MaintenanceEvent.java).
Both now import `jakarta.persistence.*` — the migration was applied uniformly across the
entire domain layer by OpenRewrite.

---

### 3.3 — CI Pipeline

Open [`.github/workflows/ci.yml`](../.github/workflows/ci.yml). The `setup-java` step now
targets Java 25:

```yaml
- name: Set up Java 21        # name not yet updated — cosmetic only
  uses: actions/setup-java@v4
  with:
    java-version: '25'        # was '21'
    distribution: 'temurin'
    cache: maven
```

---

### 3.4 — What Did NOT Change

> _"The workflow is deliberately scoped to the platform layer. Application logic is never touched."_

Point out that the following are identical to the pre-upgrade state:

- [`ReplacementService.java`](../src/main/java/com/university/assettracker/service/ReplacementService.java) — business logic unchanged
- All Thymeleaf templates — no template changes required for the jakarta migration
- [`application.properties`](../src/main/resources/application.properties) — configuration unchanged
- All tests — pass without modification

---

## Summary

| What Bob did automatically | Time saved vs. manual |
|---|---|
| Bumped Spring Boot parent + Java version | 5 min |
| Migrated `javax` → `jakarta` across all entity classes | 15–20 min (error-prone by hand) |
| Fixed the 1 compilation error after the version bump | 10–30 min depending on the error |
| Scanned 99 dependencies for CVEs | Hours of manual advisory research |
| Patched 55 vulnerabilities with BOM overrides | 30–60 min per library, validated |
| Ran final verification build | 2 min |
| **Total workflow cost** | **~$5.11** |

For the full technical detail of each change, see [`docs/bob-java-modernization.md`](bob-java-modernization.md).  
For the manual equivalent of this upgrade, see [`docs/java25-upgrade-plan.md`](java25-upgrade-plan.md).
