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
@Table(name = "types")
@NoArgsConstructor
@AllArgsConstructor
public class Type extends AuditedEntity {

    @Column(nullable = false)
    private String name;


    @OneToMany(mappedBy = "type")
    // @EqualsAndHashCode.Exclude
    // @ToString.Exclude
    private Set<MasterProduct> product;

    @OneToMany(mappedBy = "type", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
   // @Fetch(FetchMode.SUBSELECT)
    private Set<TypeTranslation> translations = new HashSet<>();

    @Override
    protected String getSequenceName() {
        return "type_id_seq";
    }
}
