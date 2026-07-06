package com.school.timetable.controller;

import com.school.timetable.model.TeachingAssignment;
import com.school.timetable.repository.TeachingAssignmentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class TeachingAssignmentController {

    private final TeachingAssignmentRepository repository;

    @GetMapping
    public List<TeachingAssignment> getAll() {
        return repository.findAll();
    }

    @GetMapping("/teacher/{teacherId}")
    public List<TeachingAssignment> getByTeacher(@PathVariable Long teacherId) {
        return repository.findByTeacherId(teacherId);
    }

    @PostMapping
    public TeachingAssignment create(@Valid @RequestBody TeachingAssignment assignment) {
        return repository.save(assignment);
    }

    @PutMapping("/{id}")
    public TeachingAssignment update(@PathVariable Long id, @Valid @RequestBody TeachingAssignment assignment) {
        assignment.setId(id);
        return repository.save(assignment);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
