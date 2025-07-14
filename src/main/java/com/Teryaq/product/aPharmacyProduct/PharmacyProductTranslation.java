package com.Teryaq.product.aPharmacyProduct;

import com.Teryaq.language.Language;
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
public class PharmacyProductTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tradeName;
    private String scientificName;
    private String notes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private PharmacyProduct product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "language_id")
    private Language language;

    // public PharmacyProductTranslation(Long id, String tradeName, String scientificName, String notes, PharmacyProduct product, Language language) {
    //     this.id = id;
    //     this.tradeName = tradeName;
    //     this.scientificName = scientificName;
    //     this.notes = notes;
    //     this.product = product;
    //     this.language = language;
    // }
} 