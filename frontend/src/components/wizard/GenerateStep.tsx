import { useState } from 'react';
import type { TimetableSolution } from '../../types';
import { solverApi } from '../../api/client';
import TimetableView from '../timetable/TimetableView';

interface Props {
  onBack: () => void;
}

export default function GenerateStep({ onBack }: Props) {
  const [solution, setSolution] = useState<TimetableSolution | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleGenerate = async () => {
    setLoading(true);
    setError('');
    try {
      const result = await solverApi.solve();
      setSolution(result);
    } catch (e: unknown) {
      setError('Errore nella generazione. Verifica di aver inserito tutti i dati necessari.');
    } finally {
      setLoading(false);
    }
  };

  const handleExport = () => {
    solverApi.exportExcel();
  };

  return (
    <div>
      <h2>Genera Orario</h2>

      {!solution && !loading && (
        <div style={{ textAlign: 'center', padding: '2rem' }}>
          <p style={{ marginBottom: '1rem' }}>
            Tutti i dati sono stati inseriti. Clicca per generare l'orario.
          </p>
          <button className="btn btn-success" onClick={handleGenerate}>
            Genera Orario
          </button>
        </div>
      )}

      {loading && (
        <div className="loading">
          Generazione in corso... (potrebbe richiedere fino a 30 secondi)
        </div>
      )}

      {error && <p style={{ color: 'red', textAlign: 'center' }}>{error}</p>}

      {solution && (
        <div>
          <div style={{ textAlign: 'center', marginBottom: '1rem' }}>
            <p>
              Score: Hard = {solution.score?.hardScore ?? '?'}, Soft = {solution.score?.softScore ?? '?'}
            </p>
            {solution.score && solution.score.hardScore < 0 && (
              <p style={{ color: 'red' }}>
                Attenzione: ci sono {Math.abs(solution.score.hardScore)} conflitti non risolvibili.
                Verifica i dati inseriti.
              </p>
            )}
            <button className="btn btn-success" onClick={handleExport} style={{ marginRight: '1rem' }}>
              Scarica Excel
            </button>
            <button className="btn btn-primary" onClick={handleGenerate}>
              Rigenera
            </button>
          </div>
          <TimetableView solution={solution} />
        </div>
      )}

      <div className="wizard-nav">
        <button className="btn btn-secondary" onClick={onBack}>Indietro</button>
        <div />
      </div>
    </div>
  );
}
