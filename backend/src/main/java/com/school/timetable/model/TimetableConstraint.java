package com.school.timetable.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "timetable_constraint")
@Data
@NoArgsConstructor
public class TimetableConstraint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ConstraintType type;

    @Enumerated(EnumType.STRING)
    private ConstraintScope scope;

    @Enumerated(EnumType.STRING)
    private ConstraintCategory category;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher; // null if scope is GLOBAL

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject; // optional, for subject-specific constraints

    // JSON string with constraint-specific parameters
    @Column(columnDefinition = "TEXT")
    private String parameters;

    private String description; // human-readable description
}
