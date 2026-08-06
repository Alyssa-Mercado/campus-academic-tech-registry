package com.university.assettracker.domain;

public enum WarrantyStatus {

    ACTIVE("Active", "bg-success"),
    EXPIRING_SOON("Expiring Soon", "bg-warning text-dark"),
    EXPIRED("Expired", "bg-danger");

    private final String displayName;
    private final String badgeClass;

    WarrantyStatus(String displayName, String badgeClass) {
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
