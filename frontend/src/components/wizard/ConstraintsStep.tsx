import { useState, useEffect } from 'react';
import type { TimetableConstraint, ConstraintCategory, ConstraintType, Teacher, Subject } from '../../types';
import { constraintApi, teacherApi, subjectApi } from '../../api/client';
import HelpTooltip from '../common/HelpTooltip';

interface Props {
  onNext: () => void;
  onBack: () => void;
  scope: 'GLOBAL' | 'TEACHER';
}

const CATEGORY_LABELS: Record<ConstraintCategory, string> = {
  DAY_OFF: 'Giorno libero',
  AVAILABILITY: 'Disponibilità oraria',
  CONSECUTIVE_HOURS: 'Ore consecutive',
  FIRST_LAST_HOUR: 'Prima/Ultima ora',
  MAX_HOURS_DAY: 'Max ore al giorno',
  NO_GAPS: 'Nessun buco',
};

const DAY_NAMES = ['Lunedì', 'Martedì', 'Mercoledì', 'Giovedì', 'Venerdì', 'Sabato'];

export default function ConstraintsStep({ onNext, onBack, scope }: Props) {
  const [constraints, setConstraints] = useState<TimetableConstraint[]>([]);
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [category, setCategory] = useState<ConstraintCategory>('DAY_OFF');
  const [type, setType] = useState<ConstraintType>('MANDATORY');
  const [selectedTeacher, setSelectedTeacher] = useState<number | null>(null);
  const [description, setDescription] = useState('');

  // Day off params
  const [dayOffDay, setDayOffDay] = useState(0);

  // Availability params
  const [day, setDay] = useState(0);
  const [fromHour, setFromHour] = useState(0);
  const [toHour, setToHour] = useState(5);

  // Consecutive hours params
  const [minConsecutive, setMinConsecutive] = useState(3);
  const [selectedSubject, setSelectedSubject] = useState<number | null>(null);

  // Max hours params
  const [maxHours, setMaxHours] = useState(5);

  useEffect(() => {
    constraintApi.getAll().then(all => {
      setConstraints(all.filter(c => c.scope === scope));
    });
    teacherApi.getAll().then(setTeachers);
    subjectApi.getAll().then(setSubjects);
  }, [scope]);

  const buildParameters = (): string => {
    switch (category) {
      case 'DAY_OFF':
        return JSON.stringify({ day: dayOffDay });
      case 'AVAILABILITY':
        return JSON.stringify({ day, fromHour, toHour, available: false });
      case 'CONSECUTIVE_HOURS':
        return JSON.stringify({ minConsecutive, subjectId: selectedSubject });
      case 'FIRST_LAST_HOUR':
        return JSON.stringify({ requireFirst: true, requireLast: true });
      case 'MAX_HOURS_DAY':
        return JSON.stringify({ maxHours });
      case 'NO_GAPS':
        return JSON.stringify({});
      default:
        return '{}';
    }
  };

  const [errors, setErrors] = useState<string[]>([]);

  const validate = (): string[] => {
    const errs: string[] = [];
    if (scope === 'TEACHER' && !selectedTeacher) {
      errs.push('Seleziona un docente');
    }
    if (!description.trim()) {
      errs.push('Inserisci una descrizione del vincolo');
    }
    if (category === 'CONSECUTIVE_HOURS' && minConsecutive < 2) {
      errs.push('Le ore consecutive devono essere almeno 2');
    }
    if (category === 'MAX_HOURS_DAY' && (maxHours < 1 || maxHours > 6)) {
      errs.push('Il massimo ore al giorno deve essere tra 1 e 6');
    }
    return errs;
  };

  const handleAdd = async () => {
    const validationErrors = validate();
    if (validationErrors.length > 0) {
      setErrors(validationErrors);
      return;
    }
    setErrors([]);
    const constraint: TimetableConstraint = {
      type,
      scope,
      category,
      teacher: scope === 'TEACHER' && selectedTeacher
        ? teachers.find(t => t.id === selectedTeacher) || null
        : null,
      subject: selectedSubject ? subjects.find(s => s.id === selectedSubject) || null : null,
      parameters: buildParameters(),
      description,
    };
    const created = await constraintApi.create(constraint);
    setConstraints([...constraints, created]);
    setDescription('');
  };

  const handleDelete = async (id: number) => {
    await constraintApi.delete(id);
    setConstraints(constraints.filter(c => c.id !== id));
  };

  return (
    <div>
      <h2>
        {scope === 'GLOBAL' ? 'Vincoli Trasversali' : 'Vincoli per Docente'}
        {' '}
        <HelpTooltip text={
          scope === 'GLOBAL'
            ? "Vincoli che si applicano a tutti i docenti (es. ogni docente deve avere almeno una prima ora nella settimana). Possono essere obbligatori o preferenziali."
            : "Vincoli specifici per un singolo docente (es. Prof. Rossi non disponibile il mercoledì pomeriggio). Seleziona il docente e poi definisci il vincolo."
        } />
      </h2>

      <div className="constraint-form">
        {scope === 'TEACHER' && (
          <div className="form-group">
            <label>Docente</label>
            <select
              value={selectedTeacher || ''}
              onChange={e => setSelectedTeacher(Number(e.target.value) || null)}
              className={errors.includes('Seleziona un docente') ? 'input-error' : ''}
            >
              <option value="">Seleziona docente...</option>
              {teachers.map(t => (
                <option key={t.id} value={t.id}>{t.firstName} {t.lastName}</option>
              ))}
            </select>
          </div>
        )}

        <div className="form-row">
          <div className="form-group">
            <label>Tipo <HelpTooltip text="Obbligatorio: il vincolo DEVE essere rispettato, altrimenti l'orario non è valido. Preferenza: il sistema cercherà di rispettarlo ma potrebbe non riuscirci." /></label>
            <select value={type} onChange={e => setType(e.target.value as ConstraintType)}>
              <option value="MANDATORY">Obbligatorio</option>
              <option value="PREFERRED">Preferenza</option>
            </select>
          </div>
          <div className="form-group">
            <label>Categoria <HelpTooltip text="Disponibilità: giorni/ore in cui non si è disponibili. Ore consecutive: minimo ore di fila. Prima/Ultima ora: garantire la presenza a inizio o fine giornata. Max ore: limite giornaliero. Nessun buco: no ore vuote tra le lezioni." /></label>
            <select value={category} onChange={e => setCategory(e.target.value as ConstraintCategory)}>
              {Object.entries(CATEGORY_LABELS).map(([key, label]) => (
                <option key={key} value={key}>{label}</option>
              ))}
            </select>
          </div>
        </div>

        {category === 'DAY_OFF' && (
          <div className="form-group">
            <label>Giorno libero <HelpTooltip text="Il giorno in cui il docente non può (o preferisce non) lavorare." /></label>
            <select value={dayOffDay} onChange={e => setDayOffDay(Number(e.target.value))}>
              {DAY_NAMES.map((name, i) => (
                <option key={i} value={i}>{name}</option>
              ))}
            </select>
          </div>
        )}

        {category === 'AVAILABILITY' && (
          <div className="form-row">
            <div className="form-group">
              <label>Giorno</label>
              <select value={day} onChange={e => setDay(Number(e.target.value))}>
                {DAY_NAMES.map((name, i) => (
                  <option key={i} value={i}>{name}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>Dall'ora</label>
              <select value={fromHour} onChange={e => setFromHour(Number(e.target.value))}>
                {[0, 1, 2, 3, 4, 5].map(h => (
                  <option key={h} value={h}>{h + 1}ª ora</option>
                ))}
              </select>
            </div>
          </div>
        )}

        {category === 'AVAILABILITY' && (
          <div className="form-group">
            <label>Fino all'ora</label>
            <select value={toHour} onChange={e => setToHour(Number(e.target.value))}>
              {[0, 1, 2, 3, 4, 5].map(h => (
                <option key={h} value={h}>{h + 1}ª ora</option>
              ))}
            </select>
          </div>
        )}

        {category === 'CONSECUTIVE_HOURS' && (
          <div className="form-row">
            <div className="form-group">
              <label>Minimo ore consecutive</label>
              <input type="number" min={2} max={6} value={minConsecutive}
                     onChange={e => setMinConsecutive(Number(e.target.value))} />
            </div>
            <div className="form-group">
              <label>Per materia (opzionale)</label>
              <select value={selectedSubject || ''} onChange={e => setSelectedSubject(Number(e.target.value) || null)}>
                <option value="">Tutte</option>
                {subjects.map(s => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
            </div>
          </div>
        )}

        {category === 'MAX_HOURS_DAY' && (
          <div className="form-group">
            <label>Massimo ore al giorno</label>
            <input type="number" min={1} max={6} value={maxHours}
                   onChange={e => setMaxHours(Number(e.target.value))} />
          </div>
        )}

        <div className="form-group">
          <label>Descrizione <HelpTooltip text="Una descrizione leggibile del vincolo, che apparirà nella lista. Serve per ricordare a cosa si riferisce." /></label>
          <input
            value={description}
            onChange={e => setDescription(e.target.value)}
            placeholder="es. Prof. Rossi non disponibile mercoledì dalle 11"
            className={errors.includes('Inserisci una descrizione del vincolo') ? 'input-error' : ''}
          />
        </div>

        {errors.length > 0 && (
          <div className="validation-errors">
            {errors.map((err, i) => (
              <p key={i}>{err}</p>
            ))}
          </div>
        )}

        <button className="btn btn-primary" onClick={handleAdd}>
          Aggiungi Vincolo
        </button>
      </div>

      <ul className="item-list">
        {constraints.map(c => (
          <li key={c.id}>
            <div>
              <span className={`chip ${c.type === 'MANDATORY' ? 'mandatory' : 'preferred'}`}>
                {c.type === 'MANDATORY' ? 'Obbligatorio' : 'Preferenza'}
              </span>
              <span className="chip">{CATEGORY_LABELS[c.category]}</span>
              {c.teacher && <span className="chip">{c.teacher.firstName} {c.teacher.lastName}</span>}
              <br />
              {c.description}
            </div>
            <button className="btn btn-danger" onClick={() => handleDelete(c.id!)}>
              Elimina
            </button>
          </li>
        ))}
      </ul>

      <div className="wizard-nav">
        <button className="btn btn-secondary" onClick={onBack}>Indietro</button>
        <button className="btn btn-primary" onClick={onNext}>Avanti</button>
      </div>
    </div>
  );
}
