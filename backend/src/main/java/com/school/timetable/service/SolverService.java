package com.school.timetable.service;

import ai.timefold.solver.core.api.solver.SolverManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.timetable.model.*;
import com.school.timetable.repository.*;
import com.school.timetable.solver.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class SolverService {

    private final SolverManager<TimetableSolution, Long> solverManager;
    private final SchoolConfigRepository configRepo;
    private final TeachingAssignmentRepository assignmentRepo;
    private final TimetableConstraintRepository constraintRepo;
    private final ObjectMapper objectMapper;

    private TimetableSolution lastSolution;

    public TimetableSolution solve() throws ExecutionException, InterruptedException {
        TimetableSolution problem = buildProblem();

        var solverJob = solverManager.solve(1L, problem);
        lastSolution = solverJob.getFinalBestSolution();
        return lastSolution;
    }

    public TimetableSolution getLastSolution() {
        return lastSolution;
    }

    private TimetableSolution buildProblem() {
        SchoolConfig config = configRepo.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Configurazione scuola non trovata"));

        List<TeachingAssignment> assignments = assignmentRepo.findAll();
        if (assignments.isEmpty()) {
            throw new IllegalStateException("Nessuna assegnazione ore docente-classe trovata");
        }

        List<Timeslot> timeslots = generateTimeslots(config);
        List<Lesson> lessons = generateLessons(assignments);

        TimetableSolution solution = new TimetableSolution(timeslots, lessons);

        // Load constraints from DB and convert to problem facts
        loadConstraints(solution);

        return solution;
    }

    private void loadConstraints(TimetableSolution solution) {
        List<TimetableConstraint> constraints = constraintRepo.findAll();
        List<TeacherDayOff> dayOffs = new ArrayList<>();
        List<TeacherAvailability> availabilities = new ArrayList<>();
        List<TeacherConsecutiveRequirement> consecutiveReqs = new ArrayList<>();

        for (TimetableConstraint c : constraints) {
            boolean mandatory = c.getType() == ConstraintType.MANDATORY;
            Long teacherId = c.getTeacher() != null ? c.getTeacher().getId() : null;
            Long subjectId = c.getSubject() != null ? c.getSubject().getId() : null;

            try {
                JsonNode params = objectMapper.readTree(c.getParameters());

                switch (c.getCategory()) {
                    case DAY_OFF -> {
                        if (teacherId == null) break;
                        int day = params.has("day") ? params.get("day").asInt() : -1;
                        if (day >= 0) {
                            dayOffs.add(new TeacherDayOff(teacherId, day, mandatory));
                        }
                        if (params.has("days") && params.get("days").isArray()) {
                            for (JsonNode d : params.get("days")) {
                                dayOffs.add(new TeacherDayOff(teacherId, d.asInt(), mandatory));
                            }
                        }
                    }
                    case AVAILABILITY -> {
                        if (teacherId == null) break;
                        int day = params.has("day") ? params.get("day").asInt() : 0;
                        int fromHour = params.has("fromHour") ? params.get("fromHour").asInt() : 0;
                        int toHour = params.has("toHour") ? params.get("toHour").asInt() : 5;
                        availabilities.add(new TeacherAvailability(teacherId, day, fromHour, toHour, mandatory));
                    }
                    case CONSECUTIVE_HOURS -> {
                        int minConsecutive = params.has("minConsecutive") ? params.get("minConsecutive").asInt() : 2;
                        // If scope is GLOBAL, apply to all teachers
                        if (c.getScope() == ConstraintScope.GLOBAL) {
                            // Get all unique teacher IDs from lessons
                            solution.getLessons().stream()
                                    .map(Lesson::getTeacherId)
                                    .distinct()
                                    .forEach(tid -> consecutiveReqs.add(
                                            new TeacherConsecutiveRequirement(tid, subjectId, minConsecutive, mandatory)));
                        } else if (teacherId != null) {
                            consecutiveReqs.add(new TeacherConsecutiveRequirement(teacherId, subjectId, minConsecutive, mandatory));
                        }
                    }
                    default -> {
                        // Other constraint types not yet handled in solver
                    }
                }
            } catch (Exception e) {
                // Skip malformed constraints
            }
        }

        solution.setDayOffs(dayOffs);
        solution.setAvailabilities(availabilities);
        solution.setConsecutiveRequirements(consecutiveReqs);
    }

    private List<Timeslot> generateTimeslots(SchoolConfig config) {
        List<Timeslot> slots = new ArrayList<>();
        for (int day = 0; day < config.getDaysPerWeek(); day++) {
            for (int hour = 0; hour < config.getHoursPerDay(); hour++) {
                slots.add(new Timeslot(day, hour));
            }
        }
        return slots;
    }

    private List<Lesson> generateLessons(List<TeachingAssignment> assignments) {
        List<Lesson> lessons = new ArrayList<>();
        long lessonId = 1;

        for (TeachingAssignment assignment : assignments) {
            Teacher teacher = assignment.getTeacher();
            Subject subject = assignment.getSubject();
            SchoolClass sc = assignment.getSchoolClass();
            Long plessoId = sc.getPlesso() != null ? sc.getPlesso().getId() : null;

            for (int i = 0; i < assignment.getHoursPerWeek(); i++) {
                lessons.add(new Lesson(
                        lessonId++,
                        teacher.getId(),
                        teacher.getFullName(),
                        subject.getId(),
                        subject.getName(),
                        sc.getId(),
                        sc.getDisplayName(),
                        plessoId
                ));
            }
        }

        return lessons;
    }
}
