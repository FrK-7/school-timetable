package com.school.timetable.config;

import com.school.timetable.service.MigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupMigration implements ApplicationRunner {

    private final MigrationService migrationService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Esecuzione migrazione automatica...");
        String result = migrationService.migrate();
        log.info("Migrazione completata:\n{}", result);
    }
}
