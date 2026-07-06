import { useState, useEffect } from 'react';
import type { SchoolConfig } from '../../types';
import { configApi } from '../../api/client';
import HelpTooltip from '../common/HelpTooltip';

interface Props {
  onNext: () => void;
}

export default function ConfigStep({ onNext }: Props) {
  const [config, setConfig] = useState<SchoolConfig>({
    daysPerWeek: 6,
    hoursPerDay: 6,
    startTime: '08:00',
    schoolName: '',
    academicYear: '2026/2027',
  });

  useEffect(() => {
    configApi.get().then(setConfig).catch(() => {});
  }, []);

  const handleSave = async () => {
    await configApi.save(config);
    onNext();
  };

  return (
    <div>
      <h2>Configurazione Scuola</h2>
      <div className="form-group">
        <label>Nome Scuola <HelpTooltip text="Il nome del plesso scolastico. Apparirà nell'intestazione dell'orario generato." /></label>
        <input
          value={config.schoolName}
          onChange={e => setConfig({ ...config, schoolName: e.target.value })}
          placeholder="es. Scuola Media Statale G. Verdi"
        />
      </div>
      <div className="form-group">
        <label>Anno Scolastico <HelpTooltip text="L'anno scolastico di riferimento, ad esempio 2026/2027." /></label>
        <input
          value={config.academicYear}
          onChange={e => setConfig({ ...config, academicYear: e.target.value })}
          placeholder="es. 2026/2027"
        />
      </div>
      <div className="form-row">
        <div className="form-group">
          <label>Giorni a settimana <HelpTooltip text="Quanti giorni di lezione ha la settimana: 5 (lunedì-venerdì) o 6 (lunedì-sabato)." /></label>
          <select
            value={config.daysPerWeek}
            onChange={e => setConfig({ ...config, daysPerWeek: Number(e.target.value) })}
          >
            <option value={5}>5 (Lun-Ven)</option>
            <option value={6}>6 (Lun-Sab)</option>
          </select>
        </div>
        <div className="form-group">
          <label>Ore al giorno <HelpTooltip text="Il numero di ore di lezione per ogni giorno (es. 6 ore dalle 08:00 alle 14:00)." /></label>
          <select
            value={config.hoursPerDay}
            onChange={e => setConfig({ ...config, hoursPerDay: Number(e.target.value) })}
          >
            {[4, 5, 6, 7, 8].map(h => (
              <option key={h} value={h}>{h}</option>
            ))}
          </select>
        </div>
      </div>
      <div className="form-group">
        <label>Ora inizio lezioni <HelpTooltip text="L'orario della prima campanella. Le ore successive vengono calcolate in sequenza." /></label>
        <input
          type="time"
          value={config.startTime}
          onChange={e => setConfig({ ...config, startTime: e.target.value })}
        />
      </div>
      <div className="wizard-nav">
        <div />
        <button className="btn btn-primary" onClick={handleSave}>
          Avanti
        </button>
      </div>
    </div>
  );
}
