package com.school.timetable.solver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAvailability {
    private Long teacherId;
    private int day;
    private int fromHour;
    private int toHour;
    private boolean mandatory;
}
