package com.university.assettracker.domain;

public enum MaintenanceStatus {

    SCHEDULED("Scheduled", "bg-primary"),
    COMPLETED("Completed", "bg-success"),
    OVERDUE("Overdue", "bg-danger");

    private final String displayName;
    private final String badgeClass;

    MaintenanceStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
