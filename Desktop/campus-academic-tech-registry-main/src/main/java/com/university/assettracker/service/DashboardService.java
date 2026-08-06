package com.university.assettracker.service;

import com.university.assettracker.domain.AssetType;
import com.university.assettracker.domain.WarrantyStatus;
import com.university.assettracker.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final AssetRepository assetRepository;
    private final MaintenanceService maintenanceService;
    private final ReplacementService replacementService;

    public DashboardService(AssetRepository assetRepository,
                            MaintenanceService maintenanceService,
                            ReplacementService replacementService) {
        this.assetRepository = assetRepository;
        this.maintenanceService = maintenanceService;
        this.replacementService = replacementService;
    }

    public long totalAssets() {
        return assetRepository.count();
    }

    /** Returns asset count keyed by AssetType, in enum declaration order. */
    public Map<AssetType, Long> countByType() {
        Map<AssetType, Long> result = new LinkedHashMap<>();
        var all = assetRepository.findAll();
        for (AssetType type : AssetType.values()) {
            long count = all.stream().filter(a -> a.getAssetType() == type).count();
            result.put(type, count);
        }
        return result;
    }

    public long upcomingMaintenanceCount() {
        return maintenanceService.findUpcoming().size();
    }

    public long overdueMaintenanceCount() {
        return maintenanceService.findOverdue().size();
    }

    public long expiringSoonCount() {
        return assetRepository.findAll().stream()
                .filter(a -> a.getWarrantyStatus() == WarrantyStatus.EXPIRING_SOON)
                .count();
    }

    public long expiredWarrantyCount() {
        return assetRepository.findAll().stream()
                .filter(a -> a.getWarrantyStatus() == WarrantyStatus.EXPIRED)
                .count();
    }

    public long recommendationCount() {
        return replacementService.getRecommendations().size();
    }
}
