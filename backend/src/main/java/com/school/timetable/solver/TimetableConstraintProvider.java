package com.school.timetable.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

import java.util.List;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.*;

public class TimetableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[]{
                // Hard constraints
                teacherConflict(factory),
                classConflict(factory),
                noConsecutiveDifferentPlesso(factory),
                noSameClassTwiceInDay(factory),
                minimumConsecutiveHard(factory),
                teacherDayOffHard(factory),
                teacherAvailabilityHard(factory),
                maxGapsPerDayHard(factory),
                // Soft constraints (strongly recommended)
                minimumConsecutiveSoft(factory),
                teacherGapsWeighted(factory),
                compactHoursForPartTimeTeachers(factory),
                minimizePlessoChanges(factory),
                groupLessonsInSamePlesso(factory),
                teacherDayOffSoft(factory),
                teacherAvailabilitySoft(factory),
        };
    }

    // === HARD CONSTRAINTS ===

    Constraint teacherConflict(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getTeacherId),
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.lessThan(Lesson::getId))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher conflict");
    }

    Constraint classConflict(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getClassId),
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.lessThan(Lesson::getId))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Class conflict");
    }

    Constraint noConsecutiveDifferentPlesso(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(l -> l.getPlessoId() != null)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getTeacherId),
                        Joiners.equal(l -> l.getTimeslot().getDay()),
                        Joiners.lessThan(Lesson::getId))
                .filter((l1, l2) -> {
                    if (l1.getPlessoId() == null || l2.getPlessoId() == null) return false;
                    if (l1.getPlessoId().equals(l2.getPlessoId())) return false;
                    int diff = Math.abs(l1.getTimeslot().getHour() - l2.getTimeslot().getHour());
                    return diff == 1;
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Consecutive hours in different plessi");
    }

    // Hard: all lessons for the same teacher+class on the same day must be consecutive
    // i.e. no gaps allowed between them (no returning to a class after leaving)
    Constraint noSameClassTwiceInDay(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getTeacherId),
                        Joiners.equal(Lesson::getClassId),
                        Joiners.equal(l -> l.getTimeslot().getDay()),
                        Joiners.lessThan(Lesson::getId))
                .filter((l1, l2) -> {
                    int diff = Math.abs(l1.getTimeslot().getHour() - l2.getTimeslot().getHour());
                    return diff > 1;
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("No return to same class after leaving");
    }

    // Hard: for teachers with a mandatory CONSECUTIVE_HOURS requirement,
    // every lesson must have at least one adjacent same-class lesson on that day.
    // Uses a helper field on Lesson to mark which teachers have this requirement.
    Constraint minimumConsecutiveHard(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .ifExists(TeacherConsecutiveRequirement.class,
                        Joiners.equal(Lesson::getTeacherId, TeacherConsecutiveRequirement::getTeacherId),
                        Joiners.filtering((lesson, req) -> req.isMandatory()
                                && (req.getSubjectId() == null || req.getSubjectId().equals(lesson.getSubjectId()))))
                .ifNotExists(Lesson.class,
                        Joiners.equal(Lesson::getTeacherId),
                        Joiners.equal(Lesson::getClassId),
                        Joiners.equal(l -> l.getTimeslot().getDay()),
                        Joiners.filtering((l1, l2) -> !l1.getId().equals(l2.getId())
                                && Math.abs(l1.getTimeslot().getHour() - l2.getTimeslot().getHour()) == 1))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Minimum consecutive hours (mandatory)");
    }

    // Soft (heavy): same logic for preferred consecutive requirements
    Constraint minimumConsecutiveSoft(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .ifExists(TeacherConsecutiveRequirement.class,
                        Joiners.equal(Lesson::getTeacherId, TeacherConsecutiveRequirement::getTeacherId),
                        Joiners.filtering((lesson, req) -> !req.isMandatory()
                                && (req.getSubjectId() == null || req.getSubjectId().equals(lesson.getSubjectId()))))
                .ifNotExists(Lesson.class,
                        Joiners.equal(Lesson::getTeacherId),
                        Joiners.equal(Lesson::getClassId),
                        Joiners.equal(l -> l.getTimeslot().getDay()),
                        Joiners.filtering((l1, l2) -> !l1.getId().equals(l2.getId())
                                && Math.abs(l1.getTimeslot().getHour() - l2.getTimeslot().getHour()) == 1))
                .penalize(HardSoftScore.of(0, 10))
                .asConstraint("Minimum consecutive hours (preferred)");
    }

    Constraint teacherDayOffHard(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .join(TeacherDayOff.class,
                        Joiners.equal(Lesson::getTeacherId, TeacherDayOff::getTeacherId),
                        Joiners.equal(l -> l.getTimeslot().getDay(), TeacherDayOff::getDay))
                .filter((lesson, dayOff) -> dayOff.isMandatory())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher day off (mandatory)");
    }

    Constraint teacherAvailabilityHard(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .join(TeacherAvailability.class,
                        Joiners.equal(Lesson::getTeacherId, TeacherAvailability::getTeacherId),
                        Joiners.equal(l -> l.getTimeslot().getDay(), TeacherAvailability::getDay))
                .filter((lesson, avail) -> avail.isMandatory()
                        && lesson.getTimeslot().getHour() >= avail.getFromHour()
                        && lesson.getTimeslot().getHour() <= avail.getToHour())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher availability (mandatory)");
    }

    // Hard: max 2 gaps per day per teacher
    // gaps = (lastHour - firstHour + 1) - lessonCount
    Constraint maxGapsPerDayHard(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .groupBy(Lesson::getTeacherId, Lesson::getDay, toList())
                .filter((teacherId, day, lessons) -> {
                    int minH = lessons.stream().mapToInt(Lesson::getHour).min().orElse(0);
                    int maxH = lessons.stream().mapToInt(Lesson::getHour).max().orElse(0);
                    int span = maxH - minH + 1;
                    int gaps = span - lessons.size();
                    return gaps > 2;
                })
                .penalize(HardSoftScore.ONE_HARD,
                        (teacherId, day, lessons) -> {
                            int minH = lessons.stream().mapToInt(Lesson::getHour).min().orElse(0);
                            int maxH = lessons.stream().mapToInt(Lesson::getHour).max().orElse(0);
                            int span = maxH - minH + 1;
                            return (span - lessons.size()) - 2;
                        })
                .asConstraint("Max 2 gaps per day per teacher");
    }

    // === SOFT CONSTRAINTS (strongly recommended) ===

    // Minimize teacher gaps - penalize proportionally to gap size
    // A gap of 3 hours is much worse than a gap of 1 hour
    // Exception: 1 hour gap between different plessi is acceptable (travel time)
    Constraint teacherGapsWeighted(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getTeacherId),
                        Joiners.equal(l -> l.getTimeslot().getDay()),
                        Joiners.lessThan(Lesson::getId))
                .filter((l1, l2) -> {
                    int diff = Math.abs(l1.getTimeslot().getHour() - l2.getTimeslot().getHour());
                    if (diff <= 1) return false; // consecutive or same = no gap

                    // Allow 1 "travel hour" gap between different plessi
                    if (diff == 2 && l1.getPlessoId() != null && l2.getPlessoId() != null
                            && !l1.getPlessoId().equals(l2.getPlessoId())) {
                        return false;
                    }
                    return true;
                })
                .penalize(HardSoftScore.ONE_SOFT,
                        (l1, l2) -> {
                            int gap = Math.abs(l1.getTimeslot().getHour() - l2.getTimeslot().getHour()) - 1;
                            return gap * gap; // quadratic penalty: 1gap=1, 2gap=4, 3gap=9
                        })
                .asConstraint("Teacher gaps (weighted)");
    }

    // Soft: penalizes days where a teacher has only 1 or 2 hours.
    // Encourages compacting lessons into fewer days with more hours each.
    // Weight 8: stronger than gap penalty, so solver prefers full days over spread-out days.
    Constraint compactHoursForPartTimeTeachers(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .groupBy(Lesson::getTeacherId, Lesson::getDay, count())
                .filter((teacherId, day, dailyCount) -> dailyCount <= 2)
                .penalize(HardSoftScore.of(0, 8),
                        (teacherId, day, dailyCount) -> 3 - dailyCount)
                .asConstraint("Compact hours for part-time teachers");
    }

    // Minimize plesso changes in a day: penalize each pair of lessons in different plessi on same day
    Constraint minimizePlessoChanges(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(l -> l.getPlessoId() != null)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getTeacherId),
                        Joiners.equal(l -> l.getTimeslot().getDay()),
                        Joiners.lessThan(Lesson::getId))
                .filter((l1, l2) -> {
                    if (l2.getPlessoId() == null) return false;
                    return !l1.getPlessoId().equals(l2.getPlessoId());
                })
                .penalize(HardSoftScore.of(0, 3)) // weight 3 per plesso change pair
                .asConstraint("Minimize plesso changes");
    }

    // Reward grouping lessons in the same plesso together (consecutive)
    Constraint groupLessonsInSamePlesso(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(l -> l.getPlessoId() != null)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getTeacherId),
                        Joiners.equal(Lesson::getPlessoId),
                        Joiners.equal(l -> l.getTimeslot().getDay()),
                        Joiners.lessThan(Lesson::getId))
                .filter((l1, l2) -> {
                    int diff = Math.abs(l1.getTimeslot().getHour() - l2.getTimeslot().getHour());
                    return diff == 1; // consecutive in same plesso
                })
                .reward(HardSoftScore.ONE_SOFT)
                .asConstraint("Group lessons in same plesso");
    }

    Constraint teacherDayOffSoft(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .join(TeacherDayOff.class,
                        Joiners.equal(Lesson::getTeacherId, TeacherDayOff::getTeacherId),
                        Joiners.equal(l -> l.getTimeslot().getDay(), TeacherDayOff::getDay))
                .filter((lesson, dayOff) -> !dayOff.isMandatory())
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Teacher day off (preferred)");
    }

    Constraint teacherAvailabilitySoft(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .join(TeacherAvailability.class,
                        Joiners.equal(Lesson::getTeacherId, TeacherAvailability::getTeacherId),
                        Joiners.equal(l -> l.getTimeslot().getDay(), TeacherAvailability::getDay))
                .filter((lesson, avail) -> !avail.isMandatory()
                        && lesson.getTimeslot().getHour() >= avail.getFromHour()
                        && lesson.getTimeslot().getHour() <= avail.getToHour())
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Teacher availability (preferred)");
    }
}
