package com.school.timetable.solver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDayOff {
    private Long teacherId;
    private int day;
    private boolean mandatory;
}
