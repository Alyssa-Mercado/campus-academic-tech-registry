# Java 25 & Spring Boot Upgrade Plan

> **Current:** Java 21 / Spring Boot 3.2.5  
> **Target:** Java 25 / Spring Boot 3.5.x

---

## Overview

This project is a clean, well-structured Spring Boot MVC application with no external API clients, no reactive stack, and no complex annotation processing beyond Lombok. That makes the upgrade path straightforward. The modernization touches four layers:

1. **Build toolchain** — bump parent POM and JDK version, drop Lombok in favour of Java records & canonical constructors.
2. **Domain model** — convert immutable value-objects to records; replace manual builders with compact constructors.
3. **Language improvements** — adopt `mapMulti`, `List.of()`, virtual threads, and other Java 25 idioms where they reduce noise.
4. **CI pipeline** — update GitHub Actions to ship Java 25.

---

## Version Matrix

| Component | Current | Target | Notes |
|---|---|---|---|
| Java | 21 | 25 | LTS. Temurin EA build available; GA expected Sept 2025. |
| Spring Boot | 3.2.5 | 3.5.x | 3.5.0 GA released May 2025; requires Java 17+ (25 fully supported). |
| Spring Framework | 6.1.x (transitive) | 6.2.x (transitive) | Pulled in automatically by Boot 3.5. |
| Hibernate ORM | 6.4.x (transitive) | 6.6.x (transitive) | No entity mapping changes needed. |
| Thymeleaf | 3.1.x | 3.1.x | Boot 3.5 stays on Thymeleaf 3.1 — no template changes needed. |
| Lombok | present (optional) | **removed** | Replaced by records + compact constructors (Java 16+, fully stable in 25). |
| JUnit / Surefire | 5.10 / 3.x | 5.11 / 3.5 (transitive) | No test changes required. |

---

## Upgrade Phases

### Phase 1 — Toolchain (15 min)

Bump `pom.xml`. Everything else still compiles unchanged.

```diff
 <parent>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-parent</artifactId>
-    <version>3.2.5</version>
+    <version>3.5.0</version>
     <relativePath/>
 </parent>

 <properties>
-    <java.version>21</java.version>
+    <java.version>25</java.version>
 </properties>

 <dependencies>
     ...
     <!-- Remove Lombok entirely -->
-    <dependency>
-        <groupId>org.projectlombok</groupId>
-        <artifactId>lombok</artifactId>
-        <optional>true</optional>
-    </dependency>
 </dependencies>

 <build>
     <plugins>
         <plugin>
             <groupId>org.springframework.boot</groupId>
             <artifactId>spring-boot-maven-plugin</artifactId>
-            <configuration>
-                <excludes>
-                    <exclude>
-                        <groupId>org.projectlombok</groupId>
-                        <artifactId>lombok</artifactId>
-                    </exclude>
-                </excludes>
-            </configuration>
         </plugin>
     </plugins>
 </build>
```

---

### Phase 2 — Drop Lombok / Simplify Domain (30 min)

> **JPA constraint:** `Asset` and `MaintenanceEvent` are `@Entity` classes — JPA requires a public no-arg constructor and mutable setters for proxy generation. They _cannot_ become records. The hand-rolled Builder pattern they already have is correct and should be kept.
>
> `ReplacementRecommendation` is a pure value object with no JPA mapping — it **can** become a record.

#### `ReplacementRecommendation.java` → record

```diff
-public class ReplacementRecommendation {
-
-    private final Asset asset;
-    private final List<String> reasons;
-
-    public ReplacementRecommendation(Asset asset, List<String> reasons) {
-        this.asset = asset;
-        this.reasons = List.copyOf(reasons);
-    }
-
-    public Asset getAsset()          { return asset; }
-    public List<String> getReasons() { return reasons; }
-
-    public String getSeverity() {
-        return reasons.size() >= 2 ? "HIGH" : "MEDIUM";
-    }
-    public String getSeverityBadgeClass() {
-        return getSeverity().equals("HIGH") ? "bg-danger" : "bg-warning text-dark";
-    }
-}
+public record ReplacementRecommendation(Asset asset, List<String> reasons) {
+
+    // Compact canonical constructor — defensive copy
+    public ReplacementRecommendation {
+        reasons = List.copyOf(reasons);
+    }
+
+    /** HIGH if both rules fired, MEDIUM if only one. */
+    public String getSeverity() {
+        return reasons.size() >= 2 ? "HIGH" : "MEDIUM";
+    }
+
+    public String getSeverityBadgeClass() {
+        return getSeverity().equals("HIGH") ? "bg-danger" : "bg-warning text-dark";
+    }
+}
```

> **Thymeleaf note:** Thymeleaf 3.1 resolves both bare record accessor names (`asset()`) and the conventional `getAsset()` form. Templates need no changes. If integration tests show resolution failures, add explicit `getAsset()` / `getReasons()` delegate methods to the record body.

#### `ReplacementService.java` — stream cleanup with `mapMulti`

```diff
 public List<ReplacementRecommendation> getRecommendations() {
-    List<ReplacementRecommendation> result = new ArrayList<>();
-
-    assetRepository.findAll().forEach(asset -> {
-        List<String> reasons = new ArrayList<>();
-
-        int threshold = config.getThresholdForType(asset.getAssetType());
-        long age = asset.getAgeYears();
-        if (age > threshold) {
-            reasons.add(String.format(
-                    "Age exceeds %d-year threshold for %s (%d years old)",
-                    threshold, asset.getAssetType().getDisplayName(), age));
-        }
-
-        if (asset.getWarrantyStatus() == WarrantyStatus.EXPIRED
-                && !maintenanceEventRepository
-                        .findByAssetIdAndStatus(asset.getId(), MaintenanceStatus.OVERDUE)
-                        .isEmpty()) {
-            reasons.add("Expired warranty with overdue maintenance");
-        }
-
-        if (!reasons.isEmpty()) {
-            result.add(new ReplacementRecommendation(asset, reasons));
-        }
-    });
-
-    result.sort(Comparator
-            .comparing((ReplacementRecommendation r) -> r.getSeverity().equals("HIGH") ? 0 : 1)
-            .thenComparing(r -> r.getAsset().getName()));
-
-    return result;
+    return assetRepository.findAll().stream()
+        .<ReplacementRecommendation>mapMulti((asset, downstream) -> {
+            var reasons = new ArrayList<String>();
+
+            int threshold = config.getThresholdForType(asset.getAssetType());
+            long age = asset.getAgeYears();
+            if (age > threshold) {
+                reasons.add("Age exceeds %d-year threshold for %s (%d years old)"
+                    .formatted(threshold, asset.getAssetType().getDisplayName(), age));
+            }
+
+            if (asset.getWarrantyStatus() == WarrantyStatus.EXPIRED
+                    && !maintenanceEventRepository
+                        .findByAssetIdAndStatus(asset.getId(), MaintenanceStatus.OVERDUE)
+                        .isEmpty()) {
+                reasons.add("Expired warranty with overdue maintenance");
+            }
+
+            if (!reasons.isEmpty()) {
+                downstream.accept(new ReplacementRecommendation(asset, reasons));
+            }
+        })
+        .sorted(Comparator
+            .comparingInt((ReplacementRecommendation r) -> r.getSeverity().equals("HIGH") ? 0 : 1)
+            .thenComparing(r -> r.asset().getName()))
+        .toList();
 }
```

---

### Phase 3 — Java 25 Language Features (1–2 hrs)

| Feature | JEP / Status | Where to apply |
|---|---|---|
| Sequenced Collections | JEP 431 (Java 21, stable) | `DashboardService.countByType()` — `LinkedHashMap` implements `SequencedMap`; code already correct, no change needed. |
| Pattern Matching for `instanceof` | JEP 394 (Java 16, stable) | Already used in `Asset.equals()` and `MaintenanceEvent.equals()` — no change needed. |
| Switch Expressions | JEP 361 (Java 14, stable) | Already used in `MaintenanceController.list()` — no change needed. |
| Unnamed Variables `_` | JEP 456 (Java 22, stable in 25) | Minor style improvement in loops/lambdas that discard the variable. |
| Primitive Types in Patterns | JEP 488 (Java 23 preview → 25) | Could be used in future rule engine logic over primitive `long age`. |
| Virtual Threads | JEP 505 (Java 25) | Add `spring.threads.virtual.enabled=true` to `application.properties`. |
| String Templates | JEP 430 withdrawn | `String.formatted()` (already used) is the stable idiomatic alternative. |

#### `application.properties` — enable virtual threads

```diff
 spring.thymeleaf.cache=false
+
+# Java 25 / Spring Boot 3.2+ — virtual threads on Tomcat
+spring.threads.virtual.enabled=true
```

#### Controllers — `List.of()` instead of `Arrays.asList()`

```diff
-// AssetController.java (3 occurrences) and MaintenanceController.java
-model.addAttribute("assetTypes", Arrays.asList(AssetType.values()));
+model.addAttribute("assetTypes", List.of(AssetType.values()));

-model.addAttribute("maintenanceStatuses",
-    Arrays.asList(MaintenanceStatus.SCHEDULED, MaintenanceStatus.COMPLETED));
+model.addAttribute("maintenanceStatuses",
+    List.of(MaintenanceStatus.SCHEDULED, MaintenanceStatus.COMPLETED));

-// Remove: import java.util.Arrays;
+// Already has: import java.util.List;
```

---

### Phase 4 — GitHub Actions CI Update (10 min)

> **Java 25 distribution availability:** Java 25 GA is expected September 2025. Until GA, use `distribution: 'temurin'` with `java-version: '25-ea'`, or pin to `'25'` once Temurin publishes a GA build.

```diff
-      - name: Set up Java 21
-        uses: actions/setup-java@v4
-        with:
-          java-version: '21'
-          distribution: 'temurin'
-          cache: maven
+      - name: Set up Java 25
+        uses: actions/setup-java@v4
+        with:
+          java-version: '25'        # use '25-ea' until GA is published
+          distribution: 'temurin'
+          cache: maven
```

---

### Phase 5 — README Update (2 min)

```diff
-| Language  | Java 21 |
-| Framework | Spring Boot 3.2.5 |
+| Language  | Java 25 |
+| Framework | Spring Boot 3.5.x |

-**Prerequisites:** Java 21+, Maven 3
+**Prerequisites:** Java 25+, Maven 3
```

---

## Risk Register

| Risk | Likelihood | Mitigation |
|---|---|---|
| Spring Boot 3.2 → 3.5 breaking changes | Low | Boot 3.3/3.4/3.5 release notes show no breaking changes in the JPA, Thymeleaf, or Web MVC layers this app uses. Run `mvn verify` after bump. |
| Java 25 Temurin EA stability in CI | Medium | Keep a parallel matrix entry for Java 21 as fallback until Java 25 GA ships in September 2025. |
| Thymeleaf + record accessor resolution | Low | Thymeleaf 3.1 resolves bare accessor names (`asset()`) and also tries `getAsset()`. Add explicit delegate methods to the record body if integration tests fail. |
| Hibernate proxy incompatibility with records | None | `ReplacementRecommendation` is not a JPA entity — no proxy is ever created for it. |
| Lombok removal breaks compilation | None | Lombok is already declared optional and no `@Data`/`@Builder` annotations exist in the codebase — the entities already have hand-rolled builders and getters. |
| Virtual threads + H2 in-memory DB | None | H2 is thread-safe; virtual threads are transparent to JDBC. No issue for dev/test use. |

---

## Effort Summary

| Phase | Files Changed | Est. Effort | Priority |
|---|---|---|---|
| 1 — pom.xml bump | `pom.xml` | 5 min | **Required** |
| 2 — Record conversion + stream cleanup | `ReplacementRecommendation.java`, `ReplacementService.java` | 20 min | Recommended |
| 3a — Virtual threads property | `application.properties` | 2 min | Recommended |
| 3b — `List.of()` cleanup | `AssetController.java`, `MaintenanceController.java` | 5 min | Optional |
| 4 — CI pipeline | `.github/workflows/ci.yml` | 5 min | **Required** |
| 5 — README update | `README.md` | 2 min | **Required** |

**Total estimated time: ~40 minutes** for all required + recommended changes.

> The codebase is already well-aligned with modern Java idioms (pattern matching in `equals()`, switch expressions in controllers, `toList()` on streams, constructor injection throughout) — this is a low-risk, high-value upgrade.
