package com.golearn.myf3school_backend.infrastructure.entity;

import com.golearn.myf3school_backend.contract.enums.ParentRelationship;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parent_students",
    uniqueConstraints = @UniqueConstraint(columnNames = {"parent_id", "student_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentStudent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParentRelationship relationship; // FATHER, MOTHER

    @Column(name = "is_primary") @Builder.Default
    private Boolean isPrimary = true;
}