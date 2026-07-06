package com.school.timetable.service;

import com.school.timetable.model.Teacher;
import com.school.timetable.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeleteService {

    private final TeacherRepository teacherRepo;
    private final SubjectRepository subjectRepo;
    private final SchoolClassRepository classRepo;
    private final TimetableConstraintRepository constraintRepo;
    private final TeachingAssignmentRepository assignmentRepo;
    private final PlessoRepository plessoRepo;

    @Transactional
    public void deleteTeacher(Long id) {
        constraintRepo.findByTeacherId(id).forEach(c -> constraintRepo.delete(c));
        assignmentRepo.deleteByTeacherId(id);
        teacherRepo.deleteById(id);
    }

    @Transactional
    public void deleteSubject(Long id) {
        List<Teacher> teachers = teacherRepo.findAll();
        for (Teacher t : teachers) {
            t.getSubjects().removeIf(s -> s.getId().equals(id));
            teacherRepo.save(t);
        }
        assignmentRepo.findAll().stream()
                .filter(a -> a.getSubject().getId().equals(id))
                .forEach(a -> assignmentRepo.delete(a));
        constraintRepo.findAll().stream()
                .filter(c -> c.getSubject() != null && c.getSubject().getId().equals(id))
                .forEach(c -> constraintRepo.delete(c));
        subjectRepo.deleteById(id);
    }

    @Transactional
    public void deleteClass(Long id) {
        List<Teacher> teachers = teacherRepo.findAll();
        for (Teacher t : teachers) {
            t.getAssignedClasses().removeIf(c -> c.getId().equals(id));
            teacherRepo.save(t);
        }
        assignmentRepo.findAll().stream()
                .filter(a -> a.getSchoolClass().getId().equals(id))
                .forEach(a -> assignmentRepo.delete(a));
        classRepo.deleteById(id);
    }

    @Transactional
    public void deleteAllData() {
        constraintRepo.deleteAll();
        assignmentRepo.deleteAll();
        List<Teacher> teachers = teacherRepo.findAll();
        for (Teacher t : teachers) {
            t.getSubjects().clear();
            t.getAssignedClasses().clear();
            teacherRepo.save(t);
        }
        teacherRepo.deleteAll();
        subjectRepo.deleteAll();
        classRepo.deleteAll();
        plessoRepo.deleteAll();
    }
}
