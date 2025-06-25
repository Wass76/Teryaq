package com.Teryaq.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Employee extends User {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy;

    private String phoneNumber;

    @Column
    private LocalDate dateOfHire;

    @Column
    private LocalTime workStart;

    @Column
    private LocalTime workEnd;

    @Column
    private String pharmacyName; // For now, just a string. Can be a relation later.

    // Add more fields as needed for pharmacy context
    @Override
    protected String getSequenceName() {
        return "employee_id_seq";
    }

} 