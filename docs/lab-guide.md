# Lab Guide — Campus Academic Technology Registry

> **Audience:** Developers new to IBM Bob

This guide is split into two standalone mini-labs. Do one or both depending on what you have access to.

| Lab | What You Will Do | Time | Requires |
|---|---|---|---|
| [Lab 1 — Explore the App](#lab-1--explore-the-app-with-bob) | Run a Spring Boot app and explore it with Bob | ~20 min | Bob IDE + Java 25 + Maven |
| [Lab 2 — Java Modernization Workflow](#lab-2--java-modernization-workflow) | Upgrade the app from Java 21 → Java 25 automatically | ~25 min | Lab 1 complete + Premium Package for Java + Git |

---

## Before You Start

### Install IBM Bob

1. Download the IBM Bob IDE from **https://ibm.com/bob**
2. Install it like any desktop application (run the `.pkg` on Mac, `.exe` on Windows)
3. Open IBM Bob and sign in with your IBM w3id credentials

### Install Java 25 and Maven

The Bob IDE uses whatever Java and Maven are installed on your machine.

**Mac (recommended — installs both in two commands):**
```bash
brew install --cask temurin@25
brew install maven
```

**Windows:**
1. Download Java 25 (Temurin): **https://adoptium.net** — select `Java 25`, `Temurin`, your OS
2. Download Maven: **https://maven.apache.org/download.cgi** — download the zip, extract it, add `bin/` to your PATH

### Verify your install

Open the Bob IDE's built-in terminal (`Terminal → New Terminal`) and run:

```bash
java -version
mvn -version
```

Expected output:
```
openjdk version "25" ...
Apache Maven 3.x.x ...
```

If either is missing, install it from the links above before continuing.

---

## Lab 1 — Explore the App with Bob

> **Time:** ~20 minutes  
> **Requires:** IBM Bob IDE, Java 25, Maven 3

### Step 1 — Download the project

Click this link to download the project as a ZIP — no GitHub account required:

**https://github.com/Alyssa-Mercado/campus-academic-tech-registry/archive/refs/heads/main.zip**

Once downloaded, unzip it. You will have a folder called `campus-academic-tech-registry-main`.

---

### Step 2 — Open the project in Bob

In the Bob IDE:

`File → Open Folder` → select the `campus-academic-tech-registry-main` folder

Wait for Maven to finish importing dependencies — you will see a progress indicator at the bottom of the IDE.

---

### Step 3 — Build and run the app

Open the built-in terminal (`Terminal → New Terminal`) and run:

```bash
mvn spring-boot:run
```

The app starts in ~4 seconds. Open **http://localhost:8080** in your browser.

> The app seeds itself automatically with 25 assets and 35 maintenance events. It resets every time you restart.

---

### Step 4 — Explore the Dashboard

Open **http://localhost:8080** and verify you can see:
- [ ] **Total assets** — 25
- [ ] **By type** — Classroom PC, Projector, Smartboard, Camera, Microphone
- [ ] **Maintenance** tiles — upcoming and overdue counts
- [ ] **Warranty** tiles — expiring soon vs expired
- [ ] **Replacement recommendations** count

---

### Step 5 — Explore the Asset List

Go to **http://localhost:8080/assets**

1. Confirm 25 assets are listed
2. Filter by type — select `Projector` → verify 5 results appear
3. Search — type `Science` → verify only Science Hall assets appear

---

### Step 6 — Open an Asset Detail

Go to **http://localhost:8080/assets/1** (Dell OptiPlex 7060)

Verify:
- [ ] **Warranty badge** shows `Expired` in red
- [ ] **Age** is shown in years
- [ ] **Maintenance history** lists past and upcoming events

Try clicking **Edit** — update the serial number, save, and confirm the change appears.

---

### Step 7 — Add a New Asset

Go to **http://localhost:8080/assets/new**, fill in any values, and click **Save**.

Verify:
- [ ] You land on the new asset's detail page
- [ ] The dashboard total increments to 26

---

### Step 8 — Explore Maintenance

Go to **http://localhost:8080/maintenance**

1. **All** tab — 35 events sorted by date
2. **Upcoming** tab — future events only
3. **Overdue** tab — events flagged red
4. Click **Mark Complete** on one — confirm it flips to `Completed` instantly

---

### Step 9 — View Replacement Recommendations

Go to **http://localhost:8080/recommendations**

Verify:
- [ ] **HIGH** severity (red) — both rules fired
- [ ] **MEDIUM** severity (amber) — one rule fired

The two rules:

| Rule | Condition |
|---|---|
| **Age** | Asset age exceeds the threshold for its type (PC: 5 yr, Projector: 7 yr, Smartboard: 8 yr, Camera/Microphone: 6 yr) |
| **Warranty + Maintenance** | Warranty expired **and** at least one maintenance event is overdue |

---

### Step 10 — Ask Bob about the code

Open the Bob chat panel (click the **Bob** icon in the sidebar).

Try these prompts:

```
Why does ReplacementRecommendation use List.copyOf() in the constructor?
```

```
Why is WarrantyStatus computed at runtime instead of stored in the database?
```

```
Is there a risk that DataSeeder runs twice on startup?
```

Bob has full knowledge of the codebase and will explain the design decisions with references to the actual source files.

---

**Lab 1 complete.** The app is running and you have explored every feature. If you have the IBM Bob Premium Package for Java, continue to Lab 2.

---

## Lab 2 — Java Modernization Workflow

> **Time:** ~25 minutes  
> **Requires:** Lab 1 complete, IBM Bob Premium Package for Java, Git installed

### Before you start — confirm the Premium Package

In the Bob chat panel, type:

```
What workflows do I have available?
```

- **Java Modernization appears** → continue below
- **Java Modernization does not appear** → contact your Bob administrator to have the Premium Package for Java added to your account

---

### Step 1 — Download the Java 21 baseline

The workflow needs the project at its **original Java 21 state** — not the already-upgraded version from Lab 1.

Click this link to download the Java 21 baseline ZIP — no GitHub account required:

**https://github.com/Alyssa-Mercado/campus-academic-tech-registry/archive/refs/heads/java-21-baseline.zip**

Unzip it. You will have a folder called `campus-academic-tech-registry-java-21-baseline`.

---

### Step 2 — Open the baseline project in Bob

In the Bob IDE:

`File → Open Folder` → select `campus-academic-tech-registry-java-21-baseline`

Wait for Maven to finish importing.

---

### Step 3 — Confirm the baseline

Open the built-in terminal and run:

```bash
mvn help:evaluate -Dexpression=project.parent.version -q -DforceStdout
```

Expected output: `3.2.5`

This confirms you are on Java 21 / Spring Boot 3.2.5 — the starting point for the upgrade.

---

### Step 4 — Trigger the Java Modernization workflow

In the Bob chat panel, type:

```
Start the Java Modernization workflow
```

Bob will respond with three options. Select **Java Upgrade**.

> From here the workflow is **fully automated**. Watch the chat panel — Bob will work through each stage on its own.

---

### Step 5 — Watch Stage 1: Baseline Build

Bob compiles the project to confirm it starts clean.

**Watch for:**
```
✅ Initial build passed — Java 21 / Spring Boot 3.2.5
   No errors. No warnings. Safe to proceed.
```

---

### Step 6 — Watch Stage 2: OpenRewrite Recipes

Bob applies two automated recipes. You may see files change in your editor — this is Bob editing your code.

| Recipe | What it does |
|---|---|
| `UpgradeSpringBoot_3_5` | Bumps `pom.xml` from Spring Boot 3.2.5 → 3.5.14 and Java 21 → 25 |
| Jakarta migration | Replaces `javax.persistence.*` with `jakarta.persistence.*` in entity classes |

---

### Step 7 — Watch Stage 3: Build Error Fix

Bob recompiles and detects 1 error. It spawns a focused subagent to fix it automatically.

**Watch for:**
```
✅ Build error resolved — 1 file fixed
```

> A **subagent** is a focused AI agent Bob spins up for a specific task. It runs independently and reports back when done.

**Cost: ~$0.77**

---

### Step 8 — Watch Stage 4: Vulnerability Scan

Bob scans all 99 dependencies against the OSV vulnerability database and finds 55 CVEs. A second subagent patches all of them.

**Watch for:**
```
✅ Vulnerability remediation complete
   55 CVEs resolved. 0 remaining.
```

**Cost: ~$4.21**

---

### Step 9 — Watch Stage 5: Final Build

Bob runs a final build to confirm everything is clean.

**Watch for:**
```
✅ Modernization Complete — Java 25 / Spring Boot 3.5.14
   Known vulnerabilities: 0 (was 55)
   Errors: 0 / Warnings: 0
```

**Total workflow cost: ~$5.11**

---

### Step 10 — Review what changed

Open these files in your editor and spot the differences:

**`pom.xml`**
- Spring Boot version: `3.2.5` → `3.5.14`
- Java version: `21` → `25`
- 8 new vulnerability override properties

**`src/main/java/.../domain/Asset.java`** (line 3)
- Import: `javax.persistence.*` → `jakarta.persistence.*`

**`.github/workflows/ci.yml`**
- `java-version: '21'` → `java-version: '25'`

**`src/main/java/.../service/ReplacementService.java`**
- Unchanged — the workflow only touches the platform layer, never application logic

---

### Step 11 — Run the upgraded app

```bash
mvn spring-boot:run
```

Open **http://localhost:8080** — the app runs identically to Lab 1, now on Java 25 / Spring Boot 3.5.14 with zero known vulnerabilities.

---

**Lab 2 complete.** 🎉

## What Bob did automatically

| Task | Manual time saved |
|---|---|
| Bumped Spring Boot + Java version | 5 min |
| Migrated `javax` → `jakarta` across all entities | 15–20 min (error-prone by hand) |
| Fixed the compilation error after the version bump | 10–30 min |
| Scanned 99 dependencies for CVEs | Hours of manual research |
| Patched 55 vulnerabilities | 30–60 min per library |
| **Total** | **~$5.11 and ~15 minutes vs. hours manually** |

---

## Reference

| Document | What it covers |
|---|---|
| [`docs/bob-java-modernization.md`](bob-java-modernization.md) | Technical deep-dive: every change the workflow made |
| [`docs/java25-upgrade-plan.md`](java25-upgrade-plan.md) | What a manual upgrade would look like — 5 phases, file-level diffs |
| [`docs/demo-script.md`](demo-script.md) | Presenter talking points for the app walkthrough |
| [`requests.http`](../requests.http) | HTTP request file for every endpoint |
