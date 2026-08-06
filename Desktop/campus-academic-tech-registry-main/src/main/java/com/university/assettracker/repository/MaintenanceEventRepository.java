package com.university.assettracker.repository;

import com.university.assettracker.domain.MaintenanceEvent;
import com.university.assettracker.domain.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceEventRepository extends JpaRepository<MaintenanceEvent, Long> {

    List<MaintenanceEvent> findByAssetId(Long assetId);

    List<MaintenanceEvent> findByStatusIn(List<MaintenanceStatus> statuses);

    List<MaintenanceEvent> findByAssetIdAndStatus(Long assetId, MaintenanceStatus status);
}
