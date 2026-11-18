package com.nextfirst.plm.supplier.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "suppliers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_suppliers_supplier_number", columnNames = "supplier_number")
})
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_number", nullable = false, length = 30)
    private String supplierNumber;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "supplier_service_types",
            joinColumns = @JoinColumn(name = "supplier_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_supplier_service_type",
                    columnNames = { "supplier_id", "service_type" }))
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    @BatchSize(size = 32)
    private Set<SupplierServiceType> serviceTypes = new HashSet<>();

    @Column(name = "contact_person", length = 120)
    private String contactPerson;

    @Column(name = "email", length = 254)
    private String email;

    @Column(name = "phone", length = 40)
    private String phone;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Supplier() {
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getSupplierNumber() { return supplierNumber; }
    public void setSupplierNumber(String supplierNumber) { this.supplierNumber = supplierNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<SupplierServiceType> getServiceTypes() { return serviceTypes; }
    public void setServiceTypes(Set<SupplierServiceType> serviceTypes) {
        this.serviceTypes = serviceTypes == null ? new HashSet<>() : new HashSet<>(serviceTypes);
    }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
