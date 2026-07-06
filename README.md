# Generatore Orario Scolastico

Applicazione web self-hosted per la generazione automatica dell'orario settimanale di una scuola media. L'utente compila un form guidato (wizard) con i dati della scuola, i docenti, le materie e i vincoli, e il sistema genera un orario ottimizzato esportabile in Excel.

## Architettura

```
┌─────────────────┐       REST API       ┌────────────────────┐
│  React (Vite)   │ ◄──────────────────► │  Spring Boot 3.x   │
│  TypeScript     │                      │  Java 17+          │
└─────────────────┘                      │                    │
                                         │  Timefold Solver   │
                                         │  H2 Database       │
                                         │  Apache POI        │
                                         └────────────────────┘
```

In produzione il frontend viene compilato e servito direttamente da Spring Boot sulla porta 8080 (unico processo).

## Prerequisiti

- **Java 17** o superiore ([Adoptium](https://adoptium.net/))
- **Node.js 18** o superiore ([nodejs.org](https://nodejs.org/))

Maven viene scaricato automaticamente dal wrapper incluso (non serve installarlo).

## Avvio rapido (Windows)

Doppio click su **`start.bat`** nella root del progetto. Lo script:
1. Verifica che Java e Node.js siano disponibili
2. Installa le dipendenze frontend (`npm install`)
3. Compila il frontend (`npm run build`)
4. Copia i file statici nel backend
5. Avvia il server su **http://localhost:8080**

Per lo sviluppo con hot-reload: **`start-dev.bat`** (backend su :8080, frontend su :5173).

## Struttura progetto

```
school-timetable/
├── backend/                    # Spring Boot application
│   ├── pom.xml
│   ├── mvnw.cmd               # Maven wrapper (auto-download)
│   └── src/main/java/com/school/timetable/
│       ├── model/              # Entità JPA (Teacher, Subject, SchoolClass, ...)
│       ├── repository/         # Spring Data repositories
│       ├── controller/         # REST API endpoints
│       ├── service/            # Business logic e solver orchestration
│       ├── solver/             # Timefold: planning entities e constraints
│       ├── export/             # Generazione Excel (Apache POI)
│       └── config/             # CORS, SPA routing, exception handling
├── frontend/                   # React SPA
│   ├── src/
│   │   ├── components/wizard/  # Form wizard (9 step)
│   │   ├── components/timetable/ # Visualizzazione griglia orario
│   │   ├── api/                # Client HTTP (axios)
│   │   └── types/              # TypeScript interfaces
│   └── vite.config.ts
├── start.bat                   # Avvio produzione (singolo processo)
├── start-dev.bat               # Avvio sviluppo (2 processi)
└── README.md
```

## Flusso utente (Wizard)

| Step | Descrizione |
|------|-------------|
| 1. Configurazione | Nome scuola, anno, giorni/settimana, ore/giorno, ora inizio |
| 2. Plessi | Sedi fisiche della scuola (opzionale, per vincoli di spostamento) |
| 3. Classi | Anno + sezione + plesso (es. 1A sede centrale, 2B succursale) |
| 4. Materie | Nome + ore settimanali per ogni anno |
| 5. Docenti | Anagrafica + materie insegnate + classi assegnate |
| 6. Assegnazione Ore | Per ogni docente: quante ore/settimana per ogni classe-materia |
| 7. Vincoli Trasversali | Regole globali (es. minimo 2h consecutive per tutti) |
| 8. Vincoli Docente | Regole individuali (es. giorno libero, indisponibilità oraria) |
| 9. Genera | Esecuzione solver → visualizzazione griglia → download Excel |

## API REST

| Endpoint | Metodi | Descrizione |
|----------|--------|-------------|
| `/api/config` | GET, POST | Configurazione scuola (singleton) |
| `/api/config/reset` | DELETE | Reset completo di tutti i dati |
| `/api/plessi` | GET, POST, PUT, DELETE | Gestione plessi |
| `/api/classes` | GET, POST, PUT, DELETE | Gestione classi/sezioni |
| `/api/subjects` | GET, POST, PUT, DELETE | Gestione materie |
| `/api/teachers` | GET, POST, PUT, DELETE | Gestione docenti |
| `/api/assignments` | GET, POST, PUT, DELETE | Assegnazioni ore docente-classe |
| `/api/constraints` | GET, POST, PUT, DELETE | Vincoli (globali e per docente) |
| `/api/solver/solve` | POST | Avvia la generazione dell'orario |
| `/api/solver/solution` | GET | Recupera l'ultimo orario generato |
| `/api/solver/export` | GET | Scarica l'orario in formato Excel |
| `/api/migrate` | POST | Migrazione manuale del database |

## Vincoli del Solver

### Hard (inderogabili)
- **Teacher conflict**: un docente non può essere in due classi alla stessa ora
- **Class conflict**: una classe non può avere due docenti alla stessa ora
- **No ore consecutive in plessi diversi**: serve almeno 1 ora di stacco per spostamento
- **No ritorno nella stessa classe**: le ore nella stessa classe devono essere consecutive
- **Minimo ore consecutive** (se configurato): blocchi di almeno 2h per classe
- **Giorno libero** (se configurato come obbligatorio): il docente non lavora quel giorno
- **Indisponibilità oraria** (se configurata come obbligatoria)

### Soft (ottimizzazione)
- **Minimizzare buchi**: penalità quadratica (3h di buco = 9 punti). Tolleranza 1h per cambio plesso
- **Minimizzare cambi plesso**: meno spostamenti possibili in una giornata
- **Raggruppare ore nello stesso plesso**: premia blocchi consecutivi nello stesso edificio
- **Giorno libero preferenziale**: il solver ci prova ma può violare
- **Indisponibilità oraria preferenziale**
- **Ore consecutive preferenziali**

## Output Excel

Il file generato (`orario.xlsx`) ha il formato:

| DOCENTE | CLASSE (ORE) | LUN 1 | LUN 2 | ... | VEN 6 |
|---------|--------------|-------|-------|-----|-------|
| **ITALIANO** | | | | | |
| Mario Rossi | 1A(4h) 1B(2h) | 1A | 1A | ... | 1B |
| **MATEMATICA** | | | | | |
| ... | | | | | |

- Colonna 1: raggruppamento per materia (intestazione gialla) e nome docente
- Colonna 2: associazione classe(ore) per ogni docente
- Colonne 3+: griglia giornaliera suddivisa in sotto-colonne (1 per ora), con la classe assegnata

## Database

- **Sviluppo/Produzione**: H2 embedded (file `backend/data/timetable.mv.db`)
- Schema gestito da Hibernate `ddl-auto=update` (aggiunge tabelle/colonne senza perdere dati)
- Per resettare: eliminare `timetable.mv.db` e riavviare, oppure usare `DELETE /api/config/reset`
- Console H2 disponibile su http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./data/timetable`)

## Tecnologie

| Componente | Tecnologia | Versione |
|------------|-----------|----------|
| Backend | Spring Boot | 3.2.5 |
| Solver | Timefold (successore OptaPlanner) | 1.8.0 |
| Database | H2 | embedded |
| Excel | Apache POI | 5.2.5 |
| Frontend | React + Vite | 18.x / 5.x |
| Linguaggio FE | TypeScript | 5.5 |
| HTTP Client | Axios | 1.7 |
