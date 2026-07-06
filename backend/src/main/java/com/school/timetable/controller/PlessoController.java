package com.school.timetable.controller;

import com.school.timetable.model.Plesso;
import com.school.timetable.repository.PlessoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plessi")
@RequiredArgsConstructor
public class PlessoController {

    private final PlessoRepository repository;

    @GetMapping
    public List<Plesso> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Plesso create(@Valid @RequestBody Plesso plesso) {
        return repository.save(plesso);
    }

    @PutMapping("/{id}")
    public Plesso update(@PathVariable Long id, @Valid @RequestBody Plesso plesso) {
        plesso.setId(id);
        return repository.save(plesso);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
