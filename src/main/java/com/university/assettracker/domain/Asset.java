package com.university.assettracker.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private AssetType assetType;

    private String serialNumber;

    private String building;

    private String roomNumber;

    private LocalDate purchaseDate;

    private LocalDate warrantyExpiryDate;

    /** Per-asset override for replacement age threshold (years). Null means use the type default. */
    private Integer ageThresholdYears;

    @Column(length = 1000)
    private String notes;

    // ---- Computed / transient ------------------------------------------------

    @Transient
    public WarrantyStatus getWarrantyStatus() {
        if (warrantyExpiryDate == null) {
            return WarrantyStatus.EXPIRED;
        }
        LocalDate today = LocalDate.now();
        if (warrantyExpiryDate.isBefore(today)) {
            return WarrantyStatus.EXPIRED;
        }
        if (warrantyExpiryDate.isBefore(today.plusDays(90))) {
            return WarrantyStatus.EXPIRING_SOON;
        }
        return WarrantyStatus.ACTIVE;
    }

    @Transient
    public long getAgeYears() {
        if (purchaseDate == null) {
            return 0;
        }
        return ChronoUnit.YEARS.between(purchaseDate, LocalDate.now());
    }

    // ---- Constructors --------------------------------------------------------

    public Asset() {}

    private Asset(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.assetType = b.assetType;
        this.serialNumber = b.serialNumber;
        this.building = b.building;
        this.roomNumber = b.roomNumber;
        this.purchaseDate = b.purchaseDate;
        this.warrantyExpiryDate = b.warrantyExpiryDate;
        this.ageThresholdYears = b.ageThresholdYears;
        this.notes = b.notes;
    }

    public static Builder builder() { return new Builder(); }

    // ---- Builder -------------------------------------------------------------

    public static final class Builder {
        private Long id;
        private String name;
        private AssetType assetType;
        private String serialNumber;
        private String building;
        private String roomNumber;
        private LocalDate purchaseDate;
        private LocalDate warrantyExpiryDate;
        private Integer ageThresholdYears;
        private String notes;

        public Builder id(Long id)                              { this.id = id; return this; }
        public Builder name(String name)                        { this.name = name; return this; }
        public Builder assetType(AssetType t)                   { this.assetType = t; return this; }
        public Builder serialNumber(String sn)                  { this.serialNumber = sn; return this; }
        public Builder building(String b)                       { this.building = b; return this; }
        public Builder roomNumber(String r)                     { this.roomNumber = r; return this; }
        public Builder purchaseDate(LocalDate d)                { this.purchaseDate = d; return this; }
        public Builder warrantyExpiryDate(LocalDate d)          { this.warrantyExpiryDate = d; return this; }
        public Builder ageThresholdYears(Integer y)             { this.ageThresholdYears = y; return this; }
        public Builder notes(String n)                          { this.notes = n; return this; }
        public Asset build()                                    { return new Asset(this); }
    }

    // ---- Getters & Setters ---------------------------------------------------

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public String getName()                      { return name; }
    public void setName(String name)             { this.name = name; }

    public AssetType getAssetType()              { return assetType; }
    public void setAssetType(AssetType t)        { this.assetType = t; }

    public String getSerialNumber()              { return serialNumber; }
    public void setSerialNumber(String sn)       { this.serialNumber = sn; }

    public String getBuilding()                  { return building; }
    public void setBuilding(String b)            { this.building = b; }

    public String getRoomNumber()                { return roomNumber; }
    public void setRoomNumber(String r)          { this.roomNumber = r; }

    public LocalDate getPurchaseDate()           { return purchaseDate; }
    public void setPurchaseDate(LocalDate d)     { this.purchaseDate = d; }

    public LocalDate getWarrantyExpiryDate()     { return warrantyExpiryDate; }
    public void setWarrantyExpiryDate(LocalDate d){ this.warrantyExpiryDate = d; }

    public Integer getAgeThresholdYears()        { return ageThresholdYears; }
    public void setAgeThresholdYears(Integer y)  { this.ageThresholdYears = y; }

    public String getNotes()                     { return notes; }
    public void setNotes(String n)               { this.notes = n; }

    // ---- equals / hashCode / toString ----------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Asset a)) return false;
        return Objects.equals(id, a.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() {
        return "Asset{id=" + id + ", name='" + name + "', assetType=" + assetType + '}';
    }
}
