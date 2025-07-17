package com.Teryaq.product.aPharmacyProduct;

import com.Teryaq.product.entity.Category;
import com.Teryaq.product.entity.Form;
import com.Teryaq.product.entity.Manufacturer;
import com.Teryaq.product.entity.Type;
import com.Teryaq.user.entity.Pharmacy;

import com.Teryaq.utils.entity.AuditedEntity;
import com.Teryaq.utils.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
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

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    @Fetch(FetchMode.SUBSELECT)
//    @EqualsAndHashCode.Exclude
//    @ToString.Exclude
    private Set<PharmacyProductBarcode> barcodes = new HashSet<>();

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

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
//    @Fetch(FetchMode.SUBSELECT)
//    @EqualsAndHashCode.Exclude
//@ToString.Exclude
    private Set<PharmacyProductTranslation> translations = new HashSet<>();

    @Override
    protected String getSequenceName() {
        return "pharmacy_product_id_seq";
    }
}