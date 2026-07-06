package com.school.timetable.solver;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@PlanningSolution
@Data
@NoArgsConstructor
public class TimetableSolution {

    @ValueRangeProvider(id = "timeslotRange")
    private List<Timeslot> timeslots;

    @PlanningEntityCollectionProperty
    private List<Lesson> lessons;

    @ProblemFactCollectionProperty
    private List<TeacherDayOff> dayOffs = new ArrayList<>();

    @ProblemFactCollectionProperty
    private List<TeacherAvailability> availabilities = new ArrayList<>();

    @ProblemFactCollectionProperty
    private List<TeacherConsecutiveRequirement> consecutiveRequirements = new ArrayList<>();

    @PlanningScore
    private HardSoftScore score;

    public TimetableSolution(List<Timeslot> timeslots, List<Lesson> lessons) {
        this.timeslots = timeslots;
        this.lessons = lessons;
    }
}
