package com.school.timetable.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    @Max(3)
    @Column(name = "class_year")
    private int year; // 1, 2, 3

    @NotBlank
    private String section; // "A", "B", "C"

    @ManyToOne
    @JoinColumn(name = "plesso_id")
    private Plesso plesso;

    public String getDisplayName() {
        return year + section;
    }
}
