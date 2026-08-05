package com.university.assettracker.domain;

public enum AssetType {

    CLASSROOM_PC("Classroom PC"),
    PROJECTOR("Projector"),
    SMARTBOARD("Smartboard"),
    CAMERA("Camera"),
    MICROPHONE("Microphone");

    private final String displayName;

    AssetType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
