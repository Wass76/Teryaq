package com.Teryaq.product.entity;


import com.Teryaq.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@Table(name = "manufacturers")
@NoArgsConstructor
@AllArgsConstructor
public class Manufacturer extends AuditedEntity {

    @Column(nullable = false)
    private String name;


    @OneToMany(mappedBy = "manufacturer")
    private Set<MasterProduct> product;

    @OneToMany(mappedBy = "manufacturer", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
   // @Fetch(FetchMode.SUBSELECT)
    private Set<ManufacturerTranslation> translations = new HashSet<>();

    @Override
    protected String getSequenceName() {
        return "manufacturer_id_seq";
    }

}

