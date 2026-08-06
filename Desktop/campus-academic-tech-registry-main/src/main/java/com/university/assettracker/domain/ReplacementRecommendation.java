package com.university.assettracker.domain;

import java.util.List;

public class ReplacementRecommendation {

    private final Asset asset;
    private final List<String> reasons;

    public ReplacementRecommendation(Asset asset, List<String> reasons) {
        this.asset = asset;
        this.reasons = List.copyOf(reasons);
    }

    public Asset getAsset() {
        return asset;
    }

    public List<String> getReasons() {
        return reasons;
    }

    /** HIGH if both rules fired, MEDIUM if only one. */
    public String getSeverity() {
        return reasons.size() >= 2 ? "HIGH" : "MEDIUM";
    }

    public String getSeverityBadgeClass() {
        return getSeverity().equals("HIGH") ? "bg-danger" : "bg-warning text-dark";
    }
}
