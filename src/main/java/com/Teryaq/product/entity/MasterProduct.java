package com.Teryaq.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;


@Data
@Entity
@Table(name = "master_product")
@NoArgsConstructor
@AllArgsConstructor
public class MasterProduct {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String tradeName;
    String scientificName;
    String form;
    String concentration;
    String size;
    String manufacturer;
    float refPurchasePrice;
    float refSellingPrice;
    String activeIngredients;
    String notes;
    float tax;
    String barcode;
    String dataSource;
    Boolean requiresPrescription;

    @ManyToMany
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private Type type;

    @OneToMany(mappedBy = "product")
    private List<MasterProductTranslation> translations;



}
