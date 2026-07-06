package com.school.timetable.controller;

import com.school.timetable.model.SchoolClass;
import com.school.timetable.repository.SchoolClassRepository;
import com.school.timetable.service.DeleteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class SchoolClassController {

    private final SchoolClassRepository repository;
    private final DeleteService deleteService;

    @GetMapping
    public List<SchoolClass> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public SchoolClass create(@Valid @RequestBody SchoolClass schoolClass) {
        return repository.save(schoolClass);
    }

    @PutMapping("/{id}")
    public SchoolClass update(@PathVariable Long id, @Valid @RequestBody SchoolClass schoolClass) {
        schoolClass.setId(id);
        return repository.save(schoolClass);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        deleteService.deleteClass(id);
    }
}
