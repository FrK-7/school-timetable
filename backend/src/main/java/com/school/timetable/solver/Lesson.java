package com.school.timetable.solver;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import lombok.Data;
import lombok.NoArgsConstructor;

@PlanningEntity
@Data
@NoArgsConstructor
public class Lesson {

    @PlanningId
    private Long id;

    private Long teacherId;
    private String teacherName;
    private Long subjectId;
    private String subjectName;
    private Long classId;
    private String className;
    private Long plessoId;

    @PlanningVariable(valueRangeProviderRefs = "timeslotRange")
    private Timeslot timeslot;

    public Lesson(Long id, Long teacherId, String teacherName,
                  Long subjectId, String subjectName,
                  Long classId, String className, Long plessoId) {
        this.id = id;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.classId = classId;
        this.className = className;
        this.plessoId = plessoId;
    }

    public int getDay() {
        return timeslot != null ? timeslot.getDay() : -1;
    }

    public int getHour() {
        return timeslot != null ? timeslot.getHour() : -1;
    }
}
