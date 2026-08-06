package com.university.assettracker.config;

import com.university.assettracker.domain.AssetType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "replacement")
public class ReplacementRuleConfig {

    private Map<String, Integer> ageThresholds = new HashMap<>();

    public Map<String, Integer> getAgeThresholds() {
        return ageThresholds;
    }

    public void setAgeThresholds(Map<String, Integer> ageThresholds) {
        this.ageThresholds = ageThresholds;
    }

    /** Returns the configured threshold (years) for a given AssetType, defaulting to 7 if not set. */
    public int getThresholdForType(AssetType type) {
        return ageThresholds.getOrDefault(type.name(), 7);
    }
}
