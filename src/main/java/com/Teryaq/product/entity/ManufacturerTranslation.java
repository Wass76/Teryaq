package com.Teryaq.product.entity;


import com.Teryaq.language.Language;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "Manufacturer_Translation")
@NoArgsConstructor
@AllArgsConstructor
public class ManufacturerTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    String name;

    @ManyToOne
    @JoinColumn(name = "manufacturers_id")
    private Manufacturer manufacturer;

    @ManyToOne
    @JoinColumn(name = "language_id")
    private Language language;
}
