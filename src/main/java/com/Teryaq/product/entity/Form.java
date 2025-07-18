package com.Teryaq.product.entity;

import com.Teryaq.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// import org.hibernate.annotations.Fetch;
// import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Set;


@Entity
@Data
@Table(name = "forms")
@NoArgsConstructor
@AllArgsConstructor
public class Form extends AuditedEntity {

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "form")
    private Set<MasterProduct> product;

    @OneToMany(mappedBy = "form", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
   // @Fetch(FetchMode.SUBSELECT)
    private Set<FormTranslation> translations = new HashSet<>();

    @Override
    protected String getSequenceName() {
        return "form_id_seq";
    }
}
