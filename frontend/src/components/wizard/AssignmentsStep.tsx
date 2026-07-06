import { useState, useEffect } from 'react';
import type { TeachingAssignment, Teacher, Subject, SchoolClass } from '../../types';
import { assignmentApi, teacherApi, subjectApi, classApi } from '../../api/client';
import HelpTooltip from '../common/HelpTooltip';

interface Props {
  onNext: () => void;
  onBack: () => void;
}

export default function AssignmentsStep({ onNext, onBack }: Props) {
  const [assignments, setAssignments] = useState<TeachingAssignment[]>([]);
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [classes, setClasses] = useState<SchoolClass[]>([]);

  const [selectedTeacher, setSelectedTeacher] = useState<number | null>(null);
  const [selectedSubject, setSelectedSubject] = useState<number | null>(null);
  const [selectedClass, setSelectedClass] = useState<number | null>(null);
  const [hours, setHours] = useState(2);
  const [errors, setErrors] = useState<string[]>([]);

  useEffect(() => {
    assignmentApi.getAll().then(setAssignments);
    teacherApi.getAll().then(setTeachers);
    subjectApi.getAll().then(setSubjects);
    classApi.getAll().then(setClasses);
  }, []);

  const validate = (): string[] => {
    const errs: string[] = [];
    if (!selectedTeacher) errs.push('Seleziona un docente');
    if (!selectedSubject) errs.push('Seleziona una materia');
    if (!selectedClass) errs.push('Seleziona una classe');
    if (hours < 1) errs.push('Le ore devono essere almeno 1');
    return errs;
  };

  const handleAdd = async () => {
    const validationErrors = validate();
    if (validationErrors.length > 0) {
      setErrors(validationErrors);
      return;
    }
    setErrors([]);

    const teacher = teachers.find(t => t.id === selectedTeacher)!;
    const subject = subjects.find(s => s.id === selectedSubject)!;
    const schoolClass = classes.find(c => c.id === selectedClass)!;

    const assignment: TeachingAssignment = {
      teacher,
      subject,
      schoolClass,
      hoursPerWeek: hours,
    };

    const created = await assignmentApi.create(assignment);
    setAssignments([...assignments, created]);
  };

  const handleDelete = async (id: number) => {
    await assignmentApi.delete(id);
    setAssignments(assignments.filter(a => a.id !== id));
  };

  // Group assignments by teacher for display
  const byTeacher = assignments.reduce((acc, a) => {
    const key = a.teacher.firstName + ' ' + a.teacher.lastName;
    (acc[key] = acc[key] || []).push(a);
    return acc;
  }, {} as Record<string, TeachingAssignment[]>);

  return (
    <div>
      <h2>Assegnazione Ore <HelpTooltip text="Per ogni docente, specifica quante ore settimanali insegna in ogni classe. Esempio: Prof. Rossi - Italiano - 1A - 4 ore/settimana." /></h2>

      <div className="constraint-form">
        <div className="form-row">
          <div className="form-group">
            <label>Docente <HelpTooltip text="Il docente a cui assegnare le ore." /></label>
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
          <div className="form-group">
            <label>Materia <HelpTooltip text="La materia che il docente insegna in questa classe." /></label>
            <select
              value={selectedSubject || ''}
              onChange={e => setSelectedSubject(Number(e.target.value) || null)}
              className={errors.includes('Seleziona una materia') ? 'input-error' : ''}
            >
              <option value="">Seleziona materia...</option>
              {subjects.map(s => (
                <option key={s.id} value={s.id}>{s.name}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>Classe <HelpTooltip text="La classe in cui il docente insegna questa materia." /></label>
            <select
              value={selectedClass || ''}
              onChange={e => setSelectedClass(Number(e.target.value) || null)}
              className={errors.includes('Seleziona una classe') ? 'input-error' : ''}
            >
              <option value="">Seleziona classe...</option>
              {classes
                .sort((a, b) => a.year - b.year || a.section.localeCompare(b.section))
                .map(c => (
                  <option key={c.id} value={c.id}>
                    {c.year}{c.section}{c.plesso ? ` (${c.plesso.name})` : ''}
                  </option>
                ))}
            </select>
          </div>
          <div className="form-group">
            <label>Ore/settimana <HelpTooltip text="Quante ore settimanali il docente insegna questa materia in questa classe." /></label>
            <input
              type="number"
              min={1}
              max={10}
              value={hours}
              onChange={e => setHours(Number(e.target.value))}
            />
          </div>
        </div>

        {errors.length > 0 && (
          <div className="validation-errors">
            {errors.map((err, i) => <p key={i}>{err}</p>)}
          </div>
        )}

        <button className="btn btn-primary" onClick={handleAdd}>
          Aggiungi Assegnazione
        </button>
      </div>

      {Object.entries(byTeacher).sort(([a], [b]) => a.localeCompare(b)).map(([teacherName, teacherAssignments]) => (
        <div key={teacherName} style={{ marginTop: '1rem' }}>
          <h4>{teacherName}</h4>
          <ul className="item-list">
            {teacherAssignments.map(a => (
              <li key={a.id}>
                <span>
                  <strong>{a.subject.name}</strong> — {a.schoolClass.year}{a.schoolClass.section} ({a.hoursPerWeek}h/sett.)
                </span>
                <button className="btn btn-danger" onClick={() => handleDelete(a.id!)}>Elimina</button>
              </li>
            ))}
          </ul>
        </div>
      ))}

      <div className="wizard-nav">
        <button className="btn btn-secondary" onClick={onBack}>Indietro</button>
        <button className="btn btn-primary" onClick={onNext} disabled={assignments.length === 0}>
          Avanti
        </button>
      </div>
    </div>
  );
}
