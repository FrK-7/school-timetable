package com.school.timetable.controller;

import com.school.timetable.model.SchoolConfig;
import com.school.timetable.repository.SchoolConfigRepository;
import com.school.timetable.service.DeleteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class SchoolConfigController {

    private final SchoolConfigRepository repository;
    private final DeleteService deleteService;

    @DeleteMapping("/reset")
    public void resetAll() {
        deleteService.deleteAllData();
        repository.deleteAll();
    }

    @GetMapping
    public ResponseEntity<SchoolConfig> get() {
        return repository.findAll().stream().findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public SchoolConfig save(@Valid @RequestBody SchoolConfig config) {
        // Only one config allowed - update if exists
        repository.findAll().stream().findFirst()
                .ifPresent(existing -> config.setId(existing.getId()));
        return repository.save(config);
    }
}
