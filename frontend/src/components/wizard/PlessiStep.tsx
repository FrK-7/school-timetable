import { useState, useEffect } from 'react';
import type { Plesso } from '../../types';
import { plessoApi } from '../../api/client';
import HelpTooltip from '../common/HelpTooltip';

interface Props {
  onNext: () => void;
  onBack: () => void;
}

export default function PlessiStep({ onNext, onBack }: Props) {
  const [plessi, setPlessi] = useState<Plesso[]>([]);
  const [name, setName] = useState('');
  const [address, setAddress] = useState('');

  useEffect(() => {
    plessoApi.getAll().then(setPlessi);
  }, []);

  const handleAdd = async () => {
    if (!name.trim()) return;
    const created = await plessoApi.create({ name, address });
    setPlessi([...plessi, created]);
    setName('');
    setAddress('');
  };

  const handleDelete = async (id: number) => {
    await plessoApi.delete(id);
    setPlessi(plessi.filter(p => p.id !== id));
  };

  return (
    <div>
      <h2>Plessi <HelpTooltip text="Se la scuola ha più sedi (plessi), inseriscile qui. I docenti non potranno avere ore consecutive in plessi diversi. Se hai un solo plesso puoi saltare questo step." /></h2>

      <div className="form-row">
        <div className="form-group">
          <label>Nome plesso <HelpTooltip text="Un nome identificativo per il plesso (es. Sede Centrale, Succursale Via Roma)." /></label>
          <input value={name} onChange={e => setName(e.target.value)} placeholder="es. Sede Centrale" />
        </div>
        <div className="form-group">
          <label>Indirizzo (opzionale)</label>
          <input value={address} onChange={e => setAddress(e.target.value)} placeholder="es. Via Verdi 10" />
        </div>
      </div>

      <button className="btn btn-primary" onClick={handleAdd} disabled={!name.trim()}>
        Aggiungi Plesso
      </button>

      <ul className="item-list">
        {plessi.map(p => (
          <li key={p.id}>
            <span><strong>{p.name}</strong>{p.address ? ` — ${p.address}` : ''}</span>
            <button className="btn btn-danger" onClick={() => handleDelete(p.id!)}>Elimina</button>
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
