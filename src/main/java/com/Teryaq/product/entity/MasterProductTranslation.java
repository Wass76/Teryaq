package com.Teryaq.product.entity;


import com.Teryaq.language.Language;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@Table(name = "master_product_translation")
@NoArgsConstructor
@AllArgsConstructor
public class MasterProductTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String tradeName;
    String scientificName;
    String notes;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private MasterProduct product;

    @ManyToOne
    @JoinColumn(name = "language_id")
    private Language language;

}
