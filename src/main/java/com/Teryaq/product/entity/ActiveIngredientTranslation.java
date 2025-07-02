package com.Teryaq.product.entity;


import com.Teryaq.language.Language;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "active_Ingredient_Translation")
@NoArgsConstructor
@AllArgsConstructor
public class ActiveIngredientTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    String name;

    @ManyToOne
    @JoinColumn(name = "active_Ingredient_id")
    private ActiveIngredient activeIngredient;

    @ManyToOne
    @JoinColumn(name = "language_id")
    private Language language;
}
