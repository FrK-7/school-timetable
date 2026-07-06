import { useState, useEffect } from 'react';
import type { SchoolClass, Plesso } from '../../types';
import { classApi, plessoApi } from '../../api/client';
import HelpTooltip from '../common/HelpTooltip';

interface Props {
  onNext: () => void;
  onBack: () => void;
}

export default function ClassesStep({ onNext, onBack }: Props) {
  const [classes, setClasses] = useState<SchoolClass[]>([]);
  const [plessi, setPlessi] = useState<Plesso[]>([]);
  const [year, setYear] = useState(1);
  const [section, setSection] = useState('');
  const [selectedPlesso, setSelectedPlesso] = useState<number | null>(null);

  useEffect(() => {
    classApi.getAll().then(setClasses);
    plessoApi.getAll().then(setPlessi);
  }, []);

  const handleAdd = async () => {
    if (!section.trim()) return;
    const plesso = selectedPlesso ? plessi.find(p => p.id === selectedPlesso) || null : null;
    const created = await classApi.create({ year, section: section.toUpperCase(), plesso });
    setClasses([...classes, created]);
    setSection('');
  };

  const handleDelete = async (id: number) => {
    await classApi.delete(id);
    setClasses(classes.filter(c => c.id !== id));
  };

  return (
    <div>
      <h2>Classi e Sezioni <HelpTooltip text="Inserisci tutte le classi della scuola. Per ogni anno (1°, 2°, 3°) aggiungi le sezioni presenti (A, B, C...) e indica in quale plesso si trova la classe." /></h2>
      <div className="form-row">
        <div className="form-group">
          <label>Anno <HelpTooltip text="L'anno di corso: 1° (prima media), 2° (seconda), 3° (terza)." /></label>
          <select value={year} onChange={e => setYear(Number(e.target.value))}>
            <option value={1}>1° (Prima)</option>
            <option value={2}>2° (Seconda)</option>
            <option value={3}>3° (Terza)</option>
          </select>
        </div>
        <div className="form-group">
          <label>Sezione <HelpTooltip text="La lettera della sezione (es. A, B, C). Ogni combinazione anno+sezione crea una classe (es. 1A, 2B)." /></label>
          <input
            value={section}
            onChange={e => setSection(e.target.value)}
            placeholder="es. A, B, C"
            maxLength={2}
          />
        </div>
      </div>

      {plessi.length > 0 && (
        <div className="form-group">
          <label>Plesso <HelpTooltip text="Il plesso (sede) in cui si trova fisicamente questa classe. Se non selezioni nulla, la classe non avrà vincoli di plesso." /></label>
          <select value={selectedPlesso || ''} onChange={e => setSelectedPlesso(Number(e.target.value) || null)}>
            <option value="">Nessun plesso</option>
            {plessi.map(p => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
        </div>
      )}

      <button className="btn btn-primary" onClick={handleAdd} disabled={!section.trim()}>
        Aggiungi Classe
      </button>

      <ul className="item-list">
        {classes
          .sort((a, b) => a.year - b.year || a.section.localeCompare(b.section))
          .map(c => (
            <li key={c.id}>
              <span>
                {c.year}{c.section}
                {c.plesso && <span className="chip">{c.plesso.name}</span>}
              </span>
              <button className="btn btn-danger" onClick={() => handleDelete(c.id!)}>
                Elimina
              </button>
            </li>
          ))}
      </ul>

      <div className="wizard-nav">
        <button className="btn btn-secondary" onClick={onBack}>Indietro</button>
        <button className="btn btn-primary" onClick={onNext} disabled={classes.length === 0}>
          Avanti
        </button>
      </div>
    </div>
  );
}
