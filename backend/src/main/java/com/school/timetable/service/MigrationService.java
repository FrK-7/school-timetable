package com.school.timetable.service;

import com.school.timetable.model.*;
import com.school.timetable.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MigrationService {

    private final JdbcTemplate jdbcTemplate;
    private final TeacherRepository teacherRepo;
    private final SubjectRepository subjectRepo;
    private final SchoolClassRepository classRepo;
    private final TeachingAssignmentRepository assignmentRepo;

    @Transactional
    public String migrate() {
        StringBuilder log = new StringBuilder();

        // 1. Rename column year -> class_year if needed
        try {
            List<String> columns = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'SCHOOL_CLASS'",
                    String.class);

            if (columns.contains("YEAR") && !columns.contains("CLASS_YEAR")) {
                jdbcTemplate.execute("ALTER TABLE SCHOOL_CLASS RENAME COLUMN \"YEAR\" TO CLASS_YEAR");
                log.append("Colonna YEAR rinominata in CLASS_YEAR.\n");
            } else {
                log.append("Colonna CLASS_YEAR già presente, nessuna modifica.\n");
            }
        } catch (Exception e) {
            log.append("Migrazione colonna: ").append(e.getMessage()).append("\n");
        }

        // 2. Same for subject_hours_per_year table
        try {
            List<String> columns = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'SUBJECT_HOURS_PER_YEAR'",
                    String.class);

            if (columns.contains("YEAR") && !columns.contains("CLASS_YEAR")) {
                jdbcTemplate.execute("ALTER TABLE SUBJECT_HOURS_PER_YEAR RENAME COLUMN \"YEAR\" TO CLASS_YEAR");
                log.append("Colonna YEAR in SUBJECT_HOURS_PER_YEAR rinominata in CLASS_YEAR.\n");
            }
        } catch (Exception e) {
            log.append("Migrazione subject_hours: ").append(e.getMessage()).append("\n");
        }

        // 3. Add plesso_id column if missing
        try {
            List<String> columns = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'SCHOOL_CLASS'",
                    String.class);

            if (!columns.contains("PLESSO_ID")) {
                jdbcTemplate.execute("ALTER TABLE SCHOOL_CLASS ADD COLUMN PLESSO_ID BIGINT");
                log.append("Colonna PLESSO_ID aggiunta a SCHOOL_CLASS.\n");
            }
        } catch (Exception e) {
            log.append("Plesso column: ").append(e.getMessage()).append("\n");
        }

        // 4. Generate TeachingAssignments from existing data if empty
        if (assignmentRepo.count() == 0) {
            int count = generateAssignmentsFromExistingData();
            log.append("Generate ").append(count).append(" assegnazioni ore da dati esistenti.\n");
        } else {
            log.append("Assegnazioni già presenti (").append(assignmentRepo.count()).append("), non rigenerate.\n");
        }

        return log.toString();
    }

    private int generateAssignmentsFromExistingData() {
        List<Teacher> teachers = teacherRepo.findAll();
        int count = 0;

        for (Teacher teacher : teachers) {
            if (teacher.getSubjects() == null || teacher.getAssignedClasses() == null) continue;

            for (Subject subject : teacher.getSubjects()) {
                for (SchoolClass sc : teacher.getAssignedClasses()) {
                    Integer hoursPerWeek = null;
                    if (subject.getHoursPerWeekByYear() != null) {
                        hoursPerWeek = subject.getHoursPerWeekByYear().get(sc.getYear());
                    }
                    if (hoursPerWeek == null || hoursPerWeek == 0) continue;

                    TeachingAssignment assignment = new TeachingAssignment();
                    assignment.setTeacher(teacher);
                    assignment.setSubject(subject);
                    assignment.setSchoolClass(sc);
                    assignment.setHoursPerWeek(hoursPerWeek);
                    assignmentRepo.save(assignment);
                    count++;
                }
            }
        }

        return count;
    }
}
