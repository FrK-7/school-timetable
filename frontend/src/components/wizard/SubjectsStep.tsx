import { useState, useEffect } from 'react';
import type { Subject } from '../../types';
import { subjectApi } from '../../api/client';
import HelpTooltip from '../common/HelpTooltip';

interface Props {
  onNext: () => void;
  onBack: () => void;
}

export default function SubjectsStep({ onNext, onBack }: Props) {
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [name, setName] = useState('');
  const [hours1, setHours1] = useState(0);
  const [hours2, setHours2] = useState(0);
  const [hours3, setHours3] = useState(0);

  useEffect(() => {
    subjectApi.getAll().then(setSubjects);
  }, []);

  const handleAdd = async () => {
    if (!name.trim()) return;
    const subject: Subject = {
      name,
      hoursPerWeekByYear: { 1: hours1, 2: hours2, 3: hours3 },
    };
    const created = await subjectApi.create(subject);
    setSubjects([...subjects, created]);
    setName('');
    setHours1(0);
    setHours2(0);
    setHours3(0);
  };

  const handleDelete = async (id: number) => {
    await subjectApi.delete(id);
    setSubjects(subjects.filter(s => s.id !== id));
  };

  return (
    <div>
      <h2>Materie <HelpTooltip text="Inserisci tutte le materie insegnate nella scuola e quante ore settimanali ha ciascuna per ogni anno." /></h2>
      <div className="form-group">
        <label>Nome Materia <HelpTooltip text="Il nome della disciplina così come compare nell'orario (es. Italiano, Matematica, Scienze)." /></label>
        <input
          value={name}
          onChange={e => setName(e.target.value)}
          placeholder="es. Italiano, Matematica, Inglese"
        />
      </div>
      <div className="form-row">
        <div className="form-group">
          <label>Ore/sett. in 1° <HelpTooltip text="Quante ore a settimana ha questa materia nelle classi prime. Metti 0 se non è prevista." /></label>
          <input type="number" min={0} max={10} value={hours1} onChange={e => setHours1(Number(e.target.value))} />
        </div>
        <div className="form-group">
          <label>Ore/sett. in 2° <HelpTooltip text="Quante ore a settimana ha questa materia nelle classi seconde." /></label>
          <input type="number" min={0} max={10} value={hours2} onChange={e => setHours2(Number(e.target.value))} />
        </div>
      </div>
      <div className="form-row">
        <div className="form-group">
          <label>Ore/sett. in 3° <HelpTooltip text="Quante ore a settimana ha questa materia nelle classi terze." /></label>
          <input type="number" min={0} max={10} value={hours3} onChange={e => setHours3(Number(e.target.value))} />
        </div>
        <div />
      </div>
      <button className="btn btn-primary" onClick={handleAdd}>
        Aggiungi Materia
      </button>

      <ul className="item-list">
        {subjects.map(s => (
          <li key={s.id}>
            <span>
              <strong>{s.name}</strong> — 1°: {s.hoursPerWeekByYear[1]}h, 2°: {s.hoursPerWeekByYear[2]}h, 3°: {s.hoursPerWeekByYear[3]}h
            </span>
            <button className="btn btn-danger" onClick={() => handleDelete(s.id!)}>
              Elimina
            </button>
          </li>
        ))}
      </ul>

      <div className="wizard-nav">
        <button className="btn btn-secondary" onClick={onBack}>Indietro</button>
        <button className="btn btn-primary" onClick={onNext} disabled={subjects.length === 0}>
          Avanti
        </button>
      </div>
    </div>
  );
}
