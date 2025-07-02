package com.Teryaq.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
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
    String concentration;
    String size;
    float refPurchasePrice;
    float refSellingPrice;
    String notes;
    float tax;

    @Column(nullable = false, unique = true)
    String barcode;
    String dataSource;
    Boolean requiresPrescription;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories;

    @ManyToMany
    @JoinTable(
            name = "product_active_Ingredient",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "active_ingredient_id")
    )
    private Set<ActiveIngredient> activeIngredients;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private Type type;

    @ManyToOne
    @JoinColumn(name = "form_id")
    private Form form;

    @ManyToOne
    @JoinColumn(name = "manufacturer_id")
    private Manufacturer manufacturer;


    @OneToMany(mappedBy = "product")
    private List<MasterProductTranslation> translations;



}
