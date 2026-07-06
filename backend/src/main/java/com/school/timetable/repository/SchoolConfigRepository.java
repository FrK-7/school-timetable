package com.school.timetable.repository;

import com.school.timetable.model.SchoolConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolConfigRepository extends JpaRepository<SchoolConfig, Long> {
}
