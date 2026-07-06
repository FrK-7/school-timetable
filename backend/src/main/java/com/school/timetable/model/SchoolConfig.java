package com.school.timetable.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class SchoolConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(5)
    @Max(6)
    private int daysPerWeek;

    @Min(1)
    @Max(8)
    private int hoursPerDay;

    private String startTime; // "08:00"

    private String schoolName;

    private String academicYear; // "2026/2027"
}
