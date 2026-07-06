import { useState, useEffect } from 'react';
import type { Teacher, Subject, SchoolClass } from '../../types';
import { teacherApi, subjectApi, classApi } from '../../api/client';
import HelpTooltip from '../common/HelpTooltip';

interface Props {
  onNext: () => void;
  onBack: () => void;
}

export default function TeachersStep({ onNext, onBack }: Props) {
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [classes, setClasses] = useState<SchoolClass[]>([]);
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [selectedSubjects, setSelectedSubjects] = useState<number[]>([]);
  const [selectedClasses, setSelectedClasses] = useState<number[]>([]);

  useEffect(() => {
    teacherApi.getAll().then(setTeachers);
    subjectApi.getAll().then(setSubjects);
    classApi.getAll().then(setClasses);
  }, []);

  const handleAdd = async () => {
    if (!firstName.trim() || !lastName.trim()) return;
    const teacher: Teacher = {
      firstName,
      lastName,
      subjects: subjects.filter(s => selectedSubjects.includes(s.id!)),
      assignedClasses: classes.filter(c => selectedClasses.includes(c.id!)),
    };
    const created = await teacherApi.create(teacher);
    setTeachers([...teachers, created]);
    setFirstName('');
    setLastName('');
    setSelectedSubjects([]);
    setSelectedClasses([]);
  };

  const handleDelete = async (id: number) => {
    await teacherApi.delete(id);
    setTeachers(teachers.filter(t => t.id !== id));
  };

  const toggleSubject = (id: number) => {
    setSelectedSubjects(prev =>
      prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
    );
  };

  const toggleClass = (id: number) => {
    setSelectedClasses(prev =>
      prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
    );
  };

  return (
    <div>
      <h2>Docenti <HelpTooltip text="Inserisci ogni docente con le materie che insegna e le classi in cui è assegnato. Il sistema calcolerà automaticamente le ore da piazzare." /></h2>
      <div className="form-row">
        <div className="form-group">
          <label>Nome</label>
          <input value={firstName} onChange={e => setFirstName(e.target.value)} placeholder="Mario" />
        </div>
        <div className="form-group">
          <label>Cognome</label>
          <input value={lastName} onChange={e => setLastName(e.target.value)} placeholder="Rossi" />
        </div>
      </div>

      <div className="form-group">
        <label>Materie insegnate <HelpTooltip text="Seleziona tutte le materie che questo docente insegna. Puoi selezionarne più di una." /></label>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
          {subjects.map(s => (
            <label key={s.id} style={{ cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={selectedSubjects.includes(s.id!)}
                onChange={() => toggleSubject(s.id!)}
              />{' '}
              {s.name}
            </label>
          ))}
        </div>
      </div>

      <div className="form-group">
        <label>Classi assegnate <HelpTooltip text="Seleziona le classi in cui il docente insegna. Le ore verranno distribuite in base alle materie e alle ore settimanali definite." /></label>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
          {classes
            .sort((a, b) => a.year - b.year || a.section.localeCompare(b.section))
            .map(c => (
              <label key={c.id} style={{ cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={selectedClasses.includes(c.id!)}
                  onChange={() => toggleClass(c.id!)}
                />{' '}
                {c.year}{c.section}
              </label>
            ))}
        </div>
      </div>

      <button className="btn btn-primary" onClick={handleAdd}>
        Aggiungi Docente
      </button>

      <ul className="item-list">
        {teachers.map(t => (
          <li key={t.id}>
            <div>
              <strong>{t.firstName} {t.lastName}</strong>
              <br />
              <small>
                Materie: {t.subjects?.map(s => s.name).join(', ') || '—'}
                {' | '}
                Classi: {t.assignedClasses?.map(c => `${c.year}${c.section}`).join(', ') || '—'}
              </small>
            </div>
            <button className="btn btn-danger" onClick={() => handleDelete(t.id!)}>
              Elimina
            </button>
          </li>
        ))}
      </ul>

      <div className="wizard-nav">
        <button className="btn btn-secondary" onClick={onBack}>Indietro</button>
        <button className="btn btn-primary" onClick={onNext} disabled={teachers.length === 0}>
          Avanti
        </button>
      </div>
    </div>
  );
}
