package com.school.timetable.repository;

import com.school.timetable.model.ConstraintScope;
import com.school.timetable.model.TimetableConstraint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimetableConstraintRepository extends JpaRepository<TimetableConstraint, Long> {
    List<TimetableConstraint> findByTeacherId(Long teacherId);
    List<TimetableConstraint> findByScope(ConstraintScope scope);
}
