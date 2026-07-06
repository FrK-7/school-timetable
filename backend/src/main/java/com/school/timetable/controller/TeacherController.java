package com.school.timetable.controller;

import com.school.timetable.model.Teacher;
import com.school.timetable.repository.TeacherRepository;
import com.school.timetable.service.DeleteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherRepository repository;
    private final DeleteService deleteService;

    @GetMapping
    public List<Teacher> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Teacher getById(@PathVariable Long id) {
        return repository.findById(id).orElseThrow();
    }

    @PostMapping
    public Teacher create(@Valid @RequestBody Teacher teacher) {
        return repository.save(teacher);
    }

    @PutMapping("/{id}")
    public Teacher update(@PathVariable Long id, @Valid @RequestBody Teacher teacher) {
        teacher.setId(id);
        return repository.save(teacher);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        deleteService.deleteTeacher(id);
    }
}
