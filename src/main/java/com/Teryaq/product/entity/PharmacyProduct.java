package com.Teryaq.product.entity;

import com.Teryaq.user.entity.Pharmacy;

import com.Teryaq.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "pharmacy_product")
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyProduct extends AuditedEntity {

    private String tradeName;
    private String scientificName;
    private String concentration;
    private String size;
    private float refPurchasePrice;
    private float refSellingPrice;
    private String notes;
    private float tax;

    private Boolean requiresPrescription;

    @ManyToOne
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;  

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "pharmacy_product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "type_id")
    private Type type;

    @ManyToOne
    @JoinColumn(name = "form_id")
    private Form form;

    @ManyToOne
    @JoinColumn(name = "manufacturer_id")
    private Manufacturer manufacturer;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    @Fetch(FetchMode.SUBSELECT)
//    @EqualsAndHashCode.Exclude
//    @ToString.Exclude
    private Set<PharmacyProductBarcode> barcodes = new HashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
//    @Fetch(FetchMode.SUBSELECT)
//    @EqualsAndHashCode.Exclude
//@ToString.Exclude
    private Set<PharmacyProductTranslation> translations = new HashSet<>();

    @Override
    protected String getSequenceName() {
        return "pharmacy_product_id_seq";
    }
}