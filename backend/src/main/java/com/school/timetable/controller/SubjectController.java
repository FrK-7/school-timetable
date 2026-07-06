package com.school.timetable.controller;

import com.school.timetable.model.Subject;
import com.school.timetable.repository.SubjectRepository;
import com.school.timetable.service.DeleteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectRepository repository;
    private final DeleteService deleteService;

    @GetMapping
    public List<Subject> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Subject create(@Valid @RequestBody Subject subject) {
        return repository.save(subject);
    }

    @PutMapping("/{id}")
    public Subject update(@PathVariable Long id, @Valid @RequestBody Subject subject) {
        subject.setId(id);
        return repository.save(subject);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        deleteService.deleteSubject(id);
    }
}
