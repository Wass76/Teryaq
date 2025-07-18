package com.Teryaq.product.entity;

import com.Teryaq.language.Language;
import com.Teryaq.utils.entity.AuditedEntity;
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
public class TypeTranslation extends AuditedEntity {

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Type type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    private Language language;

    @Override
    protected String getSequenceName() {
        return "type_translation_id_seq";
    }
}
