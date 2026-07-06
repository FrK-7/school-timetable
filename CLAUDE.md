# School Timetable Generator

## Panoramica

Generatore di orario settimanale per scuola media italiana. Il docente inserisce dati tramite wizard web, il sistema ottimizza con Timefold Solver e produce un Excel.

## Struttura

- `backend/` — Spring Boot 3.2.5, Java 17+, Timefold 1.8.0, H2, Apache POI
- `frontend/` — React 18, Vite 5, TypeScript, Axios

## Comandi

```bash
# Avvio produzione (Windows)
start.bat

# Avvio sviluppo (hot-reload)
start-dev.bat

# Solo backend
cd backend && ./mvnw.cmd spring-boot:run

# Solo frontend
cd frontend && npm run dev

# Build frontend
cd frontend && npm run build
```

## Convenzioni

- Backend: package `com.school.timetable` con sotto-package model/repository/controller/service/solver/export/config
- Entità JPA con Lombok (@Data, @Entity)
- REST API sotto `/api/`
- Frontend: componenti wizard in `src/components/wizard/`, ogni step è un file separato
- I vincoli del solver sono letti da DB, mai hardcoded nel ConstraintProvider
- La colonna `year` usa il nome `class_year` nel DB (parola riservata H2)
- Il database H2 è file-based in `backend/data/` (gitignored)

## Vincoli del dominio

- Scuola media: 3 anni (1°, 2°, 3°), sezioni multiple
- Orario settimanale fisso (si ripete tutto l'anno)
- Più plessi possibili (sedi distanti → vincolo spostamento)
- Vincoli utente: obbligatori (hard) e preferenziali (soft)
- Il solver deve rispettare al 100% i vincoli hard, ottimizzare i soft

## Testing

Non ci sono test unitari al momento. Per verificare l'output usare lo script `scripts/verify-timetable.md` con un modello AI.
