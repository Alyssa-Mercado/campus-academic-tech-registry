package com.university.assettracker.service;

import com.university.assettracker.domain.Asset;
import com.university.assettracker.domain.AssetType;
import com.university.assettracker.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AssetService {

    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public List<Asset> findAll() {
        return assetRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Asset::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Optional<Asset> findById(Long id) {
        return assetRepository.findById(id);
    }

    public Asset save(Asset asset) {
        return assetRepository.save(asset);
    }

    public void delete(Long id) {
        assetRepository.deleteById(id);
    }

    public List<Asset> findByType(AssetType type) {
        return assetRepository.findByAssetType(type)
                .stream()
                .sorted(Comparator.comparing(Asset::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<Asset> search(String keyword) {
        return assetRepository
                .findByBuildingContainingIgnoreCaseOrNameContainingIgnoreCase(keyword, keyword)
                .stream()
                .sorted(Comparator.comparing(Asset::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
