package com.Teryaq.product.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "categories")
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String name;

    @ManyToMany(mappedBy = "categories")
    private Set<MasterProduct> products = new HashSet<>();

    @OneToMany(mappedBy = "category")
    private List<CategoryTranslation> translations;

}

