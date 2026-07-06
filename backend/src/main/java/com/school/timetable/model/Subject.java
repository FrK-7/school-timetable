package com.school.timetable.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Entity
@Data
@NoArgsConstructor
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    // Hours per week indexed by year (1, 2, 3)
    // e.g. {1: 6, 2: 6, 3: 6} for Italiano
    @ElementCollection
    @CollectionTable(name = "subject_hours_per_year")
    @MapKeyColumn(name = "class_year")
    @Column(name = "hours_per_week")
    private Map<Integer, Integer> hoursPerWeekByYear;
}
