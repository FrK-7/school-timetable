package com.school.timetable.solver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherConsecutiveRequirement {
    private Long teacherId;
    private Long subjectId; // null = all subjects for this teacher
    private int minConsecutive;
    private boolean mandatory;
}
