package com.Teryaq.product.entity;

import com.Teryaq.language.Language;
import com.Teryaq.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@Entity
@Data
@Table(name = "pharmacy_product_translation")
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyProductTranslation extends AuditedEntity {
    private String tradeName;
    private String scientificName;
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private PharmacyProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    private Language language;

    @Override
    protected String getSequenceName() {
        return "pharmacy_product_translation_id_seq";
    }
} 