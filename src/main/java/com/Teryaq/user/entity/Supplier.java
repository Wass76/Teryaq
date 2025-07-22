package com.Teryaq.user.entity;

import com.Teryaq.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "suppliers", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@NoArgsConstructor
@AllArgsConstructor
public class Supplier extends AuditedEntity {
    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String phone;

    @Column
    private String address;

    @Column
    private String preferredCurrency; // SYP or USD

    @Override
    protected String getSequenceName() {
        return "supplier_id_seq";
    }
} 