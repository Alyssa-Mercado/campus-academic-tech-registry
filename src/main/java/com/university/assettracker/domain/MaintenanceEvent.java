package com.university.assettracker.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "maintenance_events")
public class MaintenanceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    private LocalDate scheduledDate;

    private LocalDate completedDate;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status;

    // ---- Constructors --------------------------------------------------------

    public MaintenanceEvent() {}

    private MaintenanceEvent(Builder b) {
        this.id = b.id;
        this.asset = b.asset;
        this.scheduledDate = b.scheduledDate;
        this.completedDate = b.completedDate;
        this.description = b.description;
        this.status = b.status;
    }

    public static Builder builder() { return new Builder(); }

    // ---- Builder -------------------------------------------------------------

    public static final class Builder {
        private Long id;
        private Asset asset;
        private LocalDate scheduledDate;
        private LocalDate completedDate;
        private String description;
        private MaintenanceStatus status;

        public Builder id(Long id)                           { this.id = id; return this; }
        public Builder asset(Asset a)                        { this.asset = a; return this; }
        public Builder scheduledDate(LocalDate d)            { this.scheduledDate = d; return this; }
        public Builder completedDate(LocalDate d)            { this.completedDate = d; return this; }
        public Builder description(String d)                 { this.description = d; return this; }
        public Builder status(MaintenanceStatus s)           { this.status = s; return this; }
        public MaintenanceEvent build()                      { return new MaintenanceEvent(this); }
    }

    // ---- Getters & Setters ---------------------------------------------------

    public Long getId()                            { return id; }
    public void setId(Long id)                     { this.id = id; }

    public Asset getAsset()                        { return asset; }
    public void setAsset(Asset asset)              { this.asset = asset; }

    public LocalDate getScheduledDate()            { return scheduledDate; }
    public void setScheduledDate(LocalDate d)      { this.scheduledDate = d; }

    public LocalDate getCompletedDate()            { return completedDate; }
    public void setCompletedDate(LocalDate d)      { this.completedDate = d; }

    public String getDescription()                 { return description; }
    public void setDescription(String d)           { this.description = d; }

    public MaintenanceStatus getStatus()           { return status; }
    public void setStatus(MaintenanceStatus s)     { this.status = s; }

    // ---- equals / hashCode / toString ----------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaintenanceEvent e)) return false;
        return Objects.equals(id, e.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() {
        return "MaintenanceEvent{id=" + id + ", status=" + status + '}';
    }
}
