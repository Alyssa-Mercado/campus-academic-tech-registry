package com.university.assettracker.controller;

import com.university.assettracker.domain.Asset;
import com.university.assettracker.domain.AssetType;
import com.university.assettracker.repository.MaintenanceEventRepository;
import com.university.assettracker.service.AssetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/assets")
public class AssetController {

    private final AssetService assetService;
    private final MaintenanceEventRepository maintenanceEventRepository;

    public AssetController(AssetService assetService,
                           MaintenanceEventRepository maintenanceEventRepository) {
        this.assetService = assetService;
        this.maintenanceEventRepository = maintenanceEventRepository;
    }

    // ---- List ----------------------------------------------------------------

    @GetMapping
    public String list(@RequestParam(required = false) String type,
                       @RequestParam(required = false) String search,
                       Model model) {

        List<Asset> assets;

        if (type != null && !type.isBlank()) {
            AssetType assetType = AssetType.valueOf(type);
            assets = assetService.findByType(assetType);
            model.addAttribute("selectedType", assetType);
        } else if (search != null && !search.isBlank()) {
            assets = assetService.search(search);
            model.addAttribute("searchTerm", search);
        } else {
            assets = assetService.findAll();
        }

        model.addAttribute("assets", assets);
        model.addAttribute("assetTypes", Arrays.asList(AssetType.values()));
        model.addAttribute("pageTitle", "Assets");
        return "assets/list";
    }

    // ---- Detail --------------------------------------------------------------

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Asset asset = assetService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + id));
        model.addAttribute("asset", asset);
        model.addAttribute("maintenanceEvents", maintenanceEventRepository.findByAssetId(id));
        model.addAttribute("pageTitle", asset.getName());
        return "assets/detail";
    }

    // ---- New form ------------------------------------------------------------

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("asset", new Asset());
        model.addAttribute("assetTypes", Arrays.asList(AssetType.values()));
        model.addAttribute("pageTitle", "Add Asset");
        return "assets/form";
    }

    // ---- Edit form -----------------------------------------------------------

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Asset asset = assetService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + id));
        model.addAttribute("asset", asset);
        model.addAttribute("assetTypes", Arrays.asList(AssetType.values()));
        model.addAttribute("pageTitle", "Edit Asset");
        return "assets/form";
    }

    // ---- Save (create or update) ---------------------------------------------

    @PostMapping("/save")
    public String save(@ModelAttribute Asset asset) {
        Asset saved = assetService.save(asset);
        return "redirect:/assets/" + saved.getId();
    }

    // ---- Delete --------------------------------------------------------------

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        // Delete all maintenance events for this asset first
        maintenanceEventRepository.findByAssetId(id)
                .forEach(e -> maintenanceEventRepository.delete(e));
        assetService.delete(id);
        return "redirect:/assets";
    }
}
