package com.school.timetable.controller;

import com.school.timetable.export.ExcelExporter;
import com.school.timetable.service.SolverService;
import com.school.timetable.solver.TimetableSolution;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/solver")
@RequiredArgsConstructor
public class SolverController {

    private final SolverService solverService;
    private final ExcelExporter excelExporter;

    @PostMapping("/solve")
    public TimetableSolution solve() throws ExecutionException, InterruptedException {
        return solverService.solve();
    }

    @GetMapping("/solution")
    public ResponseEntity<TimetableSolution> getSolution() {
        TimetableSolution solution = solverService.getLastSolution();
        if (solution == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(solution);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        TimetableSolution solution = solverService.getLastSolution();
        if (solution == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] excel = excelExporter.export(solution);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orario.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
