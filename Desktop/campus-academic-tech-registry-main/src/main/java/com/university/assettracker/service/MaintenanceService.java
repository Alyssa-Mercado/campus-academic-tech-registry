package com.university.assettracker.service;

import com.university.assettracker.domain.MaintenanceEvent;
import com.university.assettracker.domain.MaintenanceStatus;
import com.university.assettracker.repository.MaintenanceEventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class MaintenanceService {

    private final MaintenanceEventRepository maintenanceEventRepository;

    public MaintenanceService(MaintenanceEventRepository maintenanceEventRepository) {
        this.maintenanceEventRepository = maintenanceEventRepository;
    }

    // ---- Overdue sync helper -------------------------------------------------

    private List<MaintenanceEvent> syncAndSaveOverdue(List<MaintenanceEvent> events) {
        LocalDate today = LocalDate.now();
        List<MaintenanceEvent> changed = events.stream()
                .filter(e -> e.getStatus() == MaintenanceStatus.SCHEDULED
                        && e.getScheduledDate() != null
                        && e.getScheduledDate().isBefore(today))
                .peek(e -> e.setStatus(MaintenanceStatus.OVERDUE))
                .toList();
        if (!changed.isEmpty()) {
            maintenanceEventRepository.saveAll(changed);
        }
        return events;
    }

    // ---- Public methods ------------------------------------------------------

    public List<MaintenanceEvent> findAll() {
        List<MaintenanceEvent> events = maintenanceEventRepository.findAll();
        syncAndSaveOverdue(events);
        return events.stream()
                .sorted(Comparator.comparing(MaintenanceEvent::getScheduledDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public List<MaintenanceEvent> findByAsset(Long assetId) {
        List<MaintenanceEvent> events = maintenanceEventRepository.findByAssetId(assetId);
        syncAndSaveOverdue(events);
        return events.stream()
                .sorted(Comparator.comparing(MaintenanceEvent::getScheduledDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public List<MaintenanceEvent> findUpcoming() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(30);
        return maintenanceEventRepository.findAll().stream()
                .filter(e -> e.getStatus() == MaintenanceStatus.SCHEDULED
                        && e.getScheduledDate() != null
                        && !e.getScheduledDate().isBefore(today)
                        && !e.getScheduledDate().isAfter(limit))
                .sorted(Comparator.comparing(MaintenanceEvent::getScheduledDate))
                .toList();
    }

    public List<MaintenanceEvent> findOverdue() {
        // Run sync first so status is current, then query by OVERDUE status
        findAll();
        return maintenanceEventRepository
                .findByStatusIn(List.of(MaintenanceStatus.OVERDUE))
                .stream()
                .sorted(Comparator.comparing(MaintenanceEvent::getScheduledDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public MaintenanceEvent save(MaintenanceEvent event) {
        return maintenanceEventRepository.save(event);
    }

    public MaintenanceEvent markComplete(Long id) {
        MaintenanceEvent event = maintenanceEventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MaintenanceEvent not found: " + id));
        event.setStatus(MaintenanceStatus.COMPLETED);
        event.setCompletedDate(LocalDate.now());
        return maintenanceEventRepository.save(event);
    }
}
