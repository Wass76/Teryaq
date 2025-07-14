package com.Teryaq.product.entity;

import com.Teryaq.language.Language;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@Table(name = "type_translation")
@NoArgsConstructor
@AllArgsConstructor
public class TypeTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "type_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Type type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "language_id")
    private Language language;
}
