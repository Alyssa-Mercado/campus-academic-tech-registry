package com.university.assettracker.controller;

import com.university.assettracker.domain.MaintenanceEvent;
import com.university.assettracker.domain.MaintenanceStatus;
import com.university.assettracker.repository.MaintenanceEventRepository;
import com.university.assettracker.service.AssetService;
import com.university.assettracker.service.MaintenanceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final AssetService assetService;
    private final MaintenanceEventRepository maintenanceEventRepository;

    public MaintenanceController(MaintenanceService maintenanceService,
                                 AssetService assetService,
                                 MaintenanceEventRepository maintenanceEventRepository) {
        this.maintenanceService = maintenanceService;
        this.assetService = assetService;
        this.maintenanceEventRepository = maintenanceEventRepository;
    }

    // ---- List ----------------------------------------------------------------

    @GetMapping
    public String list(@RequestParam(required = false) String filter, Model model) {
        List<MaintenanceEvent> events = switch (filter != null ? filter : "") {
            case "upcoming" -> maintenanceService.findUpcoming();
            case "overdue"  -> maintenanceService.findOverdue();
            default         -> maintenanceService.findAll();
        };
        model.addAttribute("events", events);
        model.addAttribute("allAssets", assetService.findAll());
        model.addAttribute("filter", filter != null ? filter : "");
        model.addAttribute("pageTitle", "Maintenance");
        return "maintenance/list";
    }

    // ---- New form ------------------------------------------------------------

    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) Long assetId, Model model) {
        model.addAttribute("event", new MaintenanceEvent());
        model.addAttribute("assets", assetService.findAll());
        model.addAttribute("maintenanceStatuses",
                Arrays.asList(MaintenanceStatus.SCHEDULED, MaintenanceStatus.COMPLETED));
        model.addAttribute("preselectedAssetId", assetId);
        model.addAttribute("pageTitle", "Log Maintenance");
        return "maintenance/form";
    }

    // ---- Edit form -----------------------------------------------------------

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        MaintenanceEvent event = maintenanceEventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MaintenanceEvent not found: " + id));
        model.addAttribute("event", event);
        model.addAttribute("assets", assetService.findAll());
        model.addAttribute("maintenanceStatuses",
                Arrays.asList(MaintenanceStatus.SCHEDULED, MaintenanceStatus.COMPLETED));
        model.addAttribute("preselectedAssetId",
                event.getAsset() != null ? event.getAsset().getId() : null);
        model.addAttribute("pageTitle", "Edit Maintenance");
        return "maintenance/form";
    }

    // ---- Save (create or update) ---------------------------------------------

    @PostMapping("/save")
    public String save(@ModelAttribute MaintenanceEvent event,
                       @RequestParam(required = false) Long assetId) {
        if (assetId != null) {
            assetService.findById(assetId).ifPresent(event::setAsset);
        }
        maintenanceService.save(event);
        return "redirect:/maintenance";
    }

    // ---- Mark complete -------------------------------------------------------

    @PostMapping("/{id}/complete")
    public String markComplete(@PathVariable Long id) {
        maintenanceService.markComplete(id);
        return "redirect:/maintenance";
    }

    // ---- Delete --------------------------------------------------------------

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        maintenanceEventRepository.deleteById(id);
        return "redirect:/maintenance";
    }
}
