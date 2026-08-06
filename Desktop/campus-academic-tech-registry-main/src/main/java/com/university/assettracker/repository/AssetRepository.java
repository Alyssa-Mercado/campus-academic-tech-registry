package com.university.assettracker.repository;

import com.university.assettracker.domain.Asset;
import com.university.assettracker.domain.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findByAssetType(AssetType type);

    List<Asset> findByBuildingContainingIgnoreCaseOrNameContainingIgnoreCase(String building, String name);
}
