package com.school.timetable.controller;

import com.school.timetable.model.ConstraintScope;
import com.school.timetable.model.TimetableConstraint;
import com.school.timetable.repository.TimetableConstraintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/constraints")
@RequiredArgsConstructor
public class ConstraintController {

    private final TimetableConstraintRepository repository;

    @GetMapping
    public List<TimetableConstraint> getAll() {
        return repository.findAll();
    }

    @GetMapping("/global")
    public List<TimetableConstraint> getGlobal() {
        return repository.findByScope(ConstraintScope.GLOBAL);
    }

    @GetMapping("/teacher/{teacherId}")
    public List<TimetableConstraint> getByTeacher(@PathVariable Long teacherId) {
        return repository.findByTeacherId(teacherId);
    }

    @PostMapping
    public TimetableConstraint create(@RequestBody TimetableConstraint constraint) {
        return repository.save(constraint);
    }

    @PutMapping("/{id}")
    public TimetableConstraint update(@PathVariable Long id, @RequestBody TimetableConstraint constraint) {
        constraint.setId(id);
        return repository.save(constraint);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
