package com.Teryaq.user.entity;

import com.Teryaq.user.Enum.PharmacyType;
import com.Teryaq.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.Set;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Pharmacy extends AuditedEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String licenseNumber;

    @Column
    private String address;

    @Column
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PharmacyType type; // main or branch

    @Column
    private String openingHours;

    @Column
    private String phoneNumber;

    @OneToMany(mappedBy = "pharmacy")
    private Set<Employee> employees;

    @Override
    protected String getSequenceName() {
        return "pharmacy_id_seq";
    }

} 