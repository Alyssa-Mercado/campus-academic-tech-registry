package com.university.assettracker.service;

import com.university.assettracker.config.ReplacementRuleConfig;
import com.university.assettracker.domain.MaintenanceStatus;
import com.university.assettracker.domain.ReplacementRecommendation;
import com.university.assettracker.domain.WarrantyStatus;
import com.university.assettracker.repository.AssetRepository;
import com.university.assettracker.repository.MaintenanceEventRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReplacementService {

    private final AssetRepository assetRepository;
    private final MaintenanceEventRepository maintenanceEventRepository;
    private final ReplacementRuleConfig config;

    public ReplacementService(AssetRepository assetRepository,
                               MaintenanceEventRepository maintenanceEventRepository,
                               ReplacementRuleConfig config) {
        this.assetRepository = assetRepository;
        this.maintenanceEventRepository = maintenanceEventRepository;
        this.config = config;
    }

    public List<ReplacementRecommendation> getRecommendations() {
        List<ReplacementRecommendation> result = new ArrayList<>();

        assetRepository.findAll().forEach(asset -> {
            List<String> reasons = new ArrayList<>();

            // Rule 1: age exceeds the per-type threshold
            int threshold = config.getThresholdForType(asset.getAssetType());
            long age = asset.getAgeYears();
            if (age > threshold) {
                reasons.add(String.format(
                        "Age exceeds %d-year threshold for %s (%d years old)",
                        threshold,
                        asset.getAssetType().getDisplayName(),
                        age));
            }

            // Rule 2: expired warranty AND at least one overdue maintenance event
            if (asset.getWarrantyStatus() == WarrantyStatus.EXPIRED
                    && !maintenanceEventRepository
                            .findByAssetIdAndStatus(asset.getId(), MaintenanceStatus.OVERDUE)
                            .isEmpty()) {
                reasons.add("Expired warranty with overdue maintenance");
            }

            if (!reasons.isEmpty()) {
                result.add(new ReplacementRecommendation(asset, reasons));
            }
        });

        // HIGH first, then alphabetical by asset name within each severity group
        result.sort(Comparator
                .comparing((ReplacementRecommendation r) -> r.getSeverity().equals("HIGH") ? 0 : 1)
                .thenComparing(r -> r.getAsset().getName()));

        return result;
    }
}
