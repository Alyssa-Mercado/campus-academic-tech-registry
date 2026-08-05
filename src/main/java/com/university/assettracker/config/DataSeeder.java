package com.university.assettracker.config;

import com.university.assettracker.domain.*;
import com.university.assettracker.repository.AssetRepository;
import com.university.assettracker.repository.MaintenanceEventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AssetRepository assetRepository;
    private final MaintenanceEventRepository maintenanceEventRepository;

    public DataSeeder(AssetRepository assetRepository,
                      MaintenanceEventRepository maintenanceEventRepository) {
        this.assetRepository = assetRepository;
        this.maintenanceEventRepository = maintenanceEventRepository;
    }

    @Override
    public void run(String... args) {
        if (assetRepository.count() != 0) {
            return;
        }

        LocalDate today = LocalDate.now();

        // -------------------------------------------------------------------------
        // 25 ASSETS
        //
        // Warranty status is computed from warrantyExpiryDate at runtime:
        //   ACTIVE        = expiryDate > today + 90 days        (need ≥ 8)
        //   EXPIRING_SOON = today < expiryDate <= today + 89d   (need ≥ 6)
        //   EXPIRED       = expiryDate < today                  (need ≥ 11)
        //
        // Age thresholds (plan Sub-Task 5): PC≥5y, PROJECTOR≥7y, SMARTBOARD≥8y,
        //   CAMERA≥6y, MICROPHONE≥6y
        //
        // REPLACEMENT RULE 1 (age over threshold) — assets 1-3 below:
        //   PC purchased 6 y ago, PROJECTOR 8 y ago, SMARTBOARD 9 y ago
        //
        // REPLACEMENT RULE 2 (expired warranty + overdue maintenance) — assets 4-6
        //   will receive an OVERDUE maintenance event in the events section below.
        // -------------------------------------------------------------------------

        // ---- Science Hall -------------------------------------------------------

        // 1. PC – age > threshold (6 y > 5 y), EXPIRED warranty  [RULE 1 + RULE 2 candidate]
        Asset pc1 = save(Asset.builder()
                .name("Dell OptiPlex 7060")
                .assetType(AssetType.CLASSROOM_PC)
                .serialNumber("SN-PC-001")
                .building("Science Hall")
                .roomNumber("Room 101")
                .purchaseDate(today.minusYears(6))
                .warrantyExpiryDate(today.minusYears(3))
                .notes("Primary teaching workstation")
                .build());

        // 2. PROJECTOR – age > threshold (8 y > 7 y), EXPIRED warranty  [RULE 1]
        Asset prj1 = save(Asset.builder()
                .name("Epson EB-L200F Projector")
                .assetType(AssetType.PROJECTOR)
                .serialNumber("SN-PRJ-001")
                .building("Science Hall")
                .roomNumber("Room 101")
                .purchaseDate(today.minusYears(8))
                .warrantyExpiryDate(today.minusYears(5))
                .notes("Ceiling mounted, lamp nearing end of life")
                .build());

        // 3. SMARTBOARD – age > threshold (9 y > 8 y), EXPIRED warranty  [RULE 1]
        Asset sb1 = save(Asset.builder()
                .name("SMART Board 7086")
                .assetType(AssetType.SMARTBOARD)
                .serialNumber("SN-SB-001")
                .building("Science Hall")
                .roomNumber("Room 205")
                .purchaseDate(today.minusYears(9))
                .warrantyExpiryDate(today.minusYears(6))
                .notes("Touch calibration issues reported")
                .build());

        // 4. PC – EXPIRED warranty, will get OVERDUE maintenance  [RULE 2]
        Asset pc2 = save(Asset.builder()
                .name("HP EliteDesk 800 G6")
                .assetType(AssetType.CLASSROOM_PC)
                .serialNumber("SN-PC-002")
                .building("Science Hall")
                .roomNumber("Room 205")
                .purchaseDate(today.minusYears(4))
                .warrantyExpiryDate(today.minusMonths(8))
                .notes("Needs RAM upgrade")
                .build());

        // 5. CAMERA – EXPIRED warranty, will get OVERDUE maintenance  [RULE 2]
        Asset cam1 = save(Asset.builder()
                .name("Logitech MX Brio Camera")
                .assetType(AssetType.CAMERA)
                .serialNumber("SN-CAM-001")
                .building("Science Hall")
                .roomNumber("Lab A")
                .purchaseDate(today.minusYears(4).minusMonths(3))
                .warrantyExpiryDate(today.minusMonths(6))
                .notes("Used for hybrid lecture recordings")
                .build());

        // 6. MICROPHONE – EXPIRED warranty, will get OVERDUE maintenance  [RULE 2]
        Asset mic1 = save(Asset.builder()
                .name("Shure MX418 Microphone")
                .assetType(AssetType.MICROPHONE)
                .serialNumber("SN-MIC-001")
                .building("Science Hall")
                .roomNumber("Lab A")
                .purchaseDate(today.minusYears(3).minusMonths(6))
                .warrantyExpiryDate(today.minusMonths(14))
                .notes("Gooseneck mount loose")
                .build());

        // 7. PC – ACTIVE warranty
        Asset pc3 = save(Asset.builder()
                .name("Lenovo ThinkCentre M90q")
                .assetType(AssetType.CLASSROOM_PC)
                .serialNumber("SN-PC-003")
                .building("Science Hall")
                .roomNumber("Lab A")
                .purchaseDate(today.minusYears(1))
                .warrantyExpiryDate(today.plusYears(2))
                .notes("Latest deployment batch")
                .build());

        // 8. PROJECTOR – EXPIRING_SOON (45 days out)
        Asset prj2 = save(Asset.builder()
                .name("BenQ MH560 Projector")
                .assetType(AssetType.PROJECTOR)
                .serialNumber("SN-PRJ-002")
                .building("Science Hall")
                .roomNumber("Lecture Hall 1")
                .purchaseDate(today.minusYears(2))
                .warrantyExpiryDate(today.plusDays(45))
                .notes("3-year warranty expiring soon")
                .build());

        // 9. MICROPHONE – ACTIVE warranty
        Asset mic2 = save(Asset.builder()
                .name("Audio-Technica ATND1061")
                .assetType(AssetType.MICROPHONE)
                .serialNumber("SN-MIC-002")
                .building("Science Hall")
                .roomNumber("Lecture Hall 1")
                .purchaseDate(today.minusYears(1).minusMonths(2))
                .warrantyExpiryDate(today.plusYears(1).plusMonths(10))
                .notes("Beamforming ceiling microphone")
                .build());

        // ---- Library ------------------------------------------------------------

        // 10. PC – ACTIVE warranty
        Asset pc4 = save(Asset.builder()
                .name("Dell OptiPlex 7090")
                .assetType(AssetType.CLASSROOM_PC)
                .serialNumber("SN-PC-004")
                .building("Library")
                .roomNumber("Room 101")
                .purchaseDate(today.minusYears(1).minusMonths(4))
                .warrantyExpiryDate(today.plusYears(1).plusMonths(8))
                .notes("Student workstation cluster")
                .build());

        // 11. PROJECTOR – EXPIRED warranty
        Asset prj3 = save(Asset.builder()
                .name("Optoma EH412 Projector")
                .assetType(AssetType.PROJECTOR)
                .serialNumber("SN-PRJ-003")
                .building("Library")
                .roomNumber("Room 101")
                .purchaseDate(today.minusYears(5))
                .warrantyExpiryDate(today.minusYears(2))
                .notes("Fan noise reported")
                .build());

        // 12. SMARTBOARD – ACTIVE warranty
        Asset sb2 = save(Asset.builder()
                .name("SMART Board MX275")
                .assetType(AssetType.SMARTBOARD)
                .serialNumber("SN-SB-002")
                .building("Library")
                .roomNumber("Study Room B")
                .purchaseDate(today.minusYears(2))
                .warrantyExpiryDate(today.plusYears(3))
                .notes("Collaborative study area")
                .build());

        // 13. CAMERA – EXPIRING_SOON (60 days out)
        Asset cam2 = save(Asset.builder()
                .name("Poly Studio P15 Camera")
                .assetType(AssetType.CAMERA)
                .serialNumber("SN-CAM-002")
                .building("Library")
                .roomNumber("Study Room B")
                .purchaseDate(today.minusYears(2).minusMonths(6))
                .warrantyExpiryDate(today.plusDays(60))
                .notes("Dual microphone array built-in")
                .build());

        // 14. MICROPHONE – EXPIRED warranty
        Asset mic3 = save(Asset.builder()
                .name("Sennheiser EW 300 G4")
                .assetType(AssetType.MICROPHONE)
                .serialNumber("SN-MIC-003")
                .building("Library")
                .roomNumber("Seminar Room")
                .purchaseDate(today.minusYears(4))
                .warrantyExpiryDate(today.minusYears(1))
                .notes("Wireless handheld kit")
                .build());

        // 15. PC – EXPIRING_SOON (30 days out)
        Asset pc5 = save(Asset.builder()
                .name("HP ProDesk 400 G9")
                .assetType(AssetType.CLASSROOM_PC)
                .serialNumber("SN-PC-005")
                .building("Library")
                .roomNumber("Seminar Room")
                .purchaseDate(today.minusYears(3))
                .warrantyExpiryDate(today.plusDays(30))
                .notes("Booking kiosk workstation")
                .build());

        // 16. SMARTBOARD – EXPIRED warranty
        Asset sb3 = save(Asset.builder()
                .name("Promethean ActivPanel 9")
                .assetType(AssetType.SMARTBOARD)
                .serialNumber("SN-SB-003")
                .building("Library")
                .roomNumber("Lecture Hall 1")
                .purchaseDate(today.minusYears(5).minusMonths(6))
                .warrantyExpiryDate(today.minusMonths(18))
                .notes("Touch pen missing")
                .build());

        // 17. CAMERA – ACTIVE warranty
        Asset cam3 = save(Asset.builder()
                .name("Jabra PanaCast 50 Camera")
                .assetType(AssetType.CAMERA)
                .serialNumber("SN-CAM-003")
                .building("Library")
                .roomNumber("Lecture Hall 1")
                .purchaseDate(today.minusMonths(10))
                .warrantyExpiryDate(today.plusYears(2).plusMonths(2))
                .notes("180° panoramic view")
                .build());

        // ---- Engineering Block --------------------------------------------------

        // 18. PC – ACTIVE warranty
        Asset pc6 = save(Asset.builder()
                .name("Lenovo ThinkStation P360")
                .assetType(AssetType.CLASSROOM_PC)
                .serialNumber("SN-PC-006")
                .building("Engineering Block")
                .roomNumber("Lab A")
                .purchaseDate(today.minusMonths(8))
                .warrantyExpiryDate(today.plusYears(2).plusMonths(4))
                .notes("CAD workstation")
                .build());

        // 19. PROJECTOR – ACTIVE warranty
        Asset prj4 = save(Asset.builder()
                .name("Sony VPL-FHZ85 Projector")
                .assetType(AssetType.PROJECTOR)
                .serialNumber("SN-PRJ-004")
                .building("Engineering Block")
                .roomNumber("Lab A")
                .purchaseDate(today.minusYears(1).minusMonths(6))
                .warrantyExpiryDate(today.plusYears(1).plusMonths(6))
                .notes("4K laser projector")
                .build());

        // 20. SMARTBOARD – EXPIRING_SOON (20 days out)
        Asset sb4 = save(Asset.builder()
                .name("Newline TT-7521Q Smartboard")
                .assetType(AssetType.SMARTBOARD)
                .serialNumber("SN-SB-004")
                .building("Engineering Block")
                .roomNumber("Room 203")
                .purchaseDate(today.minusYears(3))
                .warrantyExpiryDate(today.plusDays(20))
                .notes("75-inch 4K panel")
                .build());

        // 21. CAMERA – EXPIRED warranty
        Asset cam4 = save(Asset.builder()
                .name("AVer CAM520 Pro Camera")
                .assetType(AssetType.CAMERA)
                .serialNumber("SN-CAM-004")
                .building("Engineering Block")
                .roomNumber("Room 203")
                .purchaseDate(today.minusYears(4).minusMonths(6))
                .warrantyExpiryDate(today.minusMonths(10))
                .notes("PTZ conference camera")
                .build());

        // 22. MICROPHONE – ACTIVE warranty
        Asset mic4 = save(Asset.builder()
                .name("Rode Wireless GO II")
                .assetType(AssetType.MICROPHONE)
                .serialNumber("SN-MIC-004")
                .building("Engineering Block")
                .roomNumber("Room 203")
                .purchaseDate(today.minusYears(1))
                .warrantyExpiryDate(today.plusYears(1).plusMonths(6))
                .notes("Clip-on wireless system")
                .build());

        // 23. PC – EXPIRING_SOON (75 days out)
        Asset pc7 = save(Asset.builder()
                .name("Dell Precision 3660 Tower")
                .assetType(AssetType.CLASSROOM_PC)
                .serialNumber("SN-PC-007")
                .building("Engineering Block")
                .roomNumber("Lecture Hall 1")
                .purchaseDate(today.minusYears(2).minusMonths(9))
                .warrantyExpiryDate(today.plusDays(75))
                .notes("Simulation workstation")
                .build());

        // 24. PROJECTOR – EXPIRED warranty
        Asset prj5 = save(Asset.builder()
                .name("Panasonic PT-MZ570 Projector")
                .assetType(AssetType.PROJECTOR)
                .serialNumber("SN-PRJ-005")
                .building("Engineering Block")
                .roomNumber("Lecture Hall 1")
                .purchaseDate(today.minusYears(6))
                .warrantyExpiryDate(today.minusYears(3))
                .notes("Lamp replaced once already")
                .build());

        // 25. MICROPHONE – EXPIRED warranty
        Asset mic5 = save(Asset.builder()
                .name("ClearOne CHAT 50 Microphone")
                .assetType(AssetType.MICROPHONE)
                .serialNumber("SN-MIC-005")
                .building("Engineering Block")
                .roomNumber("Lecture Hall 1")
                .purchaseDate(today.minusYears(5))
                .warrantyExpiryDate(today.minusYears(2))
                .notes("USB conference microphone")
                .build());

        // -------------------------------------------------------------------------
        // WARRANTY STATUS TALLY (computed via warrantyExpiryDate):
        //   ACTIVE (>today+90d):  pc3, pc4, sb2, cam3, pc6, prj4, mic4, mic2  = 8  ✓
        //   EXPIRING_SOON (0–89d): prj2, cam2, pc5, sb4, cam2(60d), pc7(75d)   = 6  ✓
        //   EXPIRED (<today):     pc1, prj1, sb1, pc2, cam1, mic1, prj3, mic3,
        //                         sb3, cam4, prj5, mic5                         = 12 (≥11) ✓
        // -------------------------------------------------------------------------

        // -------------------------------------------------------------------------
        // 35 MAINTENANCE EVENTS
        // -------------------------------------------------------------------------

        // === COMPLETED events (≥ 10) ============================================

        saveEvent(pc1,  today.minusMonths(6),  today.minusMonths(6),  "Annual hardware inspection",        MaintenanceStatus.COMPLETED);
        saveEvent(prj1, today.minusMonths(4),  today.minusMonths(4),  "Bulb replacement",                  MaintenanceStatus.COMPLETED);
        saveEvent(sb1,  today.minusMonths(3),  today.minusMonths(3),  "Touch calibration service",         MaintenanceStatus.COMPLETED);
        saveEvent(pc3,  today.minusMonths(2),  today.minusMonths(2),  "Software update and security patch", MaintenanceStatus.COMPLETED);
        saveEvent(prj2, today.minusMonths(5),  today.minusMonths(5),  "Lens cleaning",                     MaintenanceStatus.COMPLETED);
        saveEvent(sb2,  today.minusMonths(7),  today.minusMonths(7),  "Firmware update",                   MaintenanceStatus.COMPLETED);
        saveEvent(cam3, today.minusMonths(1),  today.minusMonths(1),  "Camera driver update",              MaintenanceStatus.COMPLETED);
        saveEvent(pc6,  today.minusWeeks(6),   today.minusWeeks(6),   "Hardware diagnostic",               MaintenanceStatus.COMPLETED);
        saveEvent(prj4, today.minusMonths(3),  today.minusMonths(3),  "Optical alignment check",           MaintenanceStatus.COMPLETED);
        saveEvent(mic2, today.minusMonths(2),  today.minusMonths(2),  "Audio level calibration",           MaintenanceStatus.COMPLETED);
        saveEvent(pc4,  today.minusWeeks(8),   today.minusWeeks(8),   "OS reimaging",                      MaintenanceStatus.COMPLETED);

        // === SCHEDULED future events (≥ 10) =====================================

        saveEvent(pc1,  today.plusDays(7),   null, "Quarterly software update",       MaintenanceStatus.SCHEDULED);
        saveEvent(prj2, today.plusDays(10),  null, "Filter cleaning",                 MaintenanceStatus.SCHEDULED);
        saveEvent(sb2,  today.plusDays(12),  null, "Pen and eraser replacement",      MaintenanceStatus.SCHEDULED);
        saveEvent(cam3, today.plusDays(15),  null, "Firmware update",                 MaintenanceStatus.SCHEDULED);
        saveEvent(pc4,  today.plusDays(18),  null, "RAM upgrade installation",        MaintenanceStatus.SCHEDULED);
        saveEvent(prj4, today.plusDays(20),  null, "Annual laser module inspection",  MaintenanceStatus.SCHEDULED);
        saveEvent(mic4, today.plusDays(5),   null, "Wireless frequency scan",         MaintenanceStatus.SCHEDULED);
        saveEvent(sb4,  today.plusDays(8),   null, "Calibration check",               MaintenanceStatus.SCHEDULED);
        saveEvent(pc6,  today.plusDays(22),  null, "GPU driver update",               MaintenanceStatus.SCHEDULED);
        saveEvent(pc7,  today.plusDays(25),  null, "Thermal paste replacement",       MaintenanceStatus.SCHEDULED);

        // === OVERDUE events (≥ 10) — past scheduledDate, status=OVERDUE ==========
        // Assets pc2, cam1, mic1 will satisfy RULE 2 (expired warranty + overdue)

        saveEvent(pc2,  today.minusDays(10), null, "Security patch rollout",          MaintenanceStatus.OVERDUE);
        saveEvent(cam1, today.minusDays(15), null, "Lens cleaning and focus check",   MaintenanceStatus.OVERDUE);
        saveEvent(mic1, today.minusDays(20), null, "Gooseneck mount tightening",      MaintenanceStatus.OVERDUE);
        saveEvent(prj3, today.minusDays(25), null, "Fan filter replacement",          MaintenanceStatus.OVERDUE);
        saveEvent(mic3, today.minusDays(30), null, "Battery and transmitter test",    MaintenanceStatus.OVERDUE);
        saveEvent(sb3,  today.minusDays(35), null, "Touch pen recalibration",         MaintenanceStatus.OVERDUE);
        saveEvent(cam4, today.minusDays(40), null, "PTZ motor lubrication",           MaintenanceStatus.OVERDUE);
        saveEvent(prj5, today.minusDays(45), null, "Lamp hour inspection",            MaintenanceStatus.OVERDUE);
        saveEvent(mic5, today.minusDays(50), null, "USB port cleaning",               MaintenanceStatus.OVERDUE);
        saveEvent(sb1,  today.minusDays(60), null, "Display panel diagnostic",        MaintenanceStatus.OVERDUE);
        saveEvent(prj1, today.minusDays(55), null, "Mounting bracket inspection",     MaintenanceStatus.OVERDUE);
        saveEvent(pc5,  today.minusDays(12), null, "Disk health check",               MaintenanceStatus.OVERDUE);
        saveEvent(cam2, today.minusDays(18), null, "Auto-focus calibration",          MaintenanceStatus.OVERDUE);

        // -------------------------------------------------------------------------
        // EVENT TALLY: 11 COMPLETED + 10 SCHEDULED + 13 OVERDUE = 34... add one more
        // -------------------------------------------------------------------------
        saveEvent(pc2,  today.minusDays(45), null, "BIOS firmware update",            MaintenanceStatus.OVERDUE);
        // Total: 11 + 10 + 14 = 35 ✓
    }

    // ---- Helpers ----------------------------------------------------------------

    private Asset save(Asset asset) {
        return assetRepository.save(asset);
    }

    private void saveEvent(Asset asset, LocalDate scheduledDate, LocalDate completedDate,
                           String description, MaintenanceStatus status) {
        maintenanceEventRepository.save(MaintenanceEvent.builder()
                .asset(asset)
                .scheduledDate(scheduledDate)
                .completedDate(completedDate)
                .description(description)
                .status(status)
                .build());
    }
}
