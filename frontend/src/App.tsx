import { useState } from 'react';
import ConfigStep from './components/wizard/ConfigStep';
import PlessiStep from './components/wizard/PlessiStep';
import ClassesStep from './components/wizard/ClassesStep';
import SubjectsStep from './components/wizard/SubjectsStep';
import TeachersStep from './components/wizard/TeachersStep';
import AssignmentsStep from './components/wizard/AssignmentsStep';
import ConstraintsStep from './components/wizard/ConstraintsStep';
import GenerateStep from './components/wizard/GenerateStep';

const STEPS = [
  'Configurazione',
  'Plessi',
  'Classi',
  'Materie',
  'Docenti',
  'Assegnazione Ore',
  'Vincoli Trasversali',
  'Vincoli Docente',
  'Genera',
];

export default function App() {
  const [currentStep, setCurrentStep] = useState(0);

  const next = () => setCurrentStep(s => Math.min(s + 1, STEPS.length - 1));
  const back = () => setCurrentStep(s => Math.max(s - 1, 0));

  return (
    <div className="app">
      <h1>Generatore Orario Scolastico</h1>

      <div className="wizard">
        <div className="wizard-steps">
          {STEPS.map((step, i) => (
            <span
              key={i}
              className={`wizard-step ${i === currentStep ? 'active' : ''} ${i < currentStep ? 'completed' : ''}`}
              onClick={() => i <= currentStep && setCurrentStep(i)}
            >
              {step}
            </span>
          ))}
        </div>

        {currentStep === 0 && <ConfigStep onNext={next} />}
        {currentStep === 1 && <PlessiStep onNext={next} onBack={back} />}
        {currentStep === 2 && <ClassesStep onNext={next} onBack={back} />}
        {currentStep === 3 && <SubjectsStep onNext={next} onBack={back} />}
        {currentStep === 4 && <TeachersStep onNext={next} onBack={back} />}
        {currentStep === 5 && <AssignmentsStep onNext={next} onBack={back} />}
        {currentStep === 6 && <ConstraintsStep onNext={next} onBack={back} scope="GLOBAL" />}
        {currentStep === 7 && <ConstraintsStep onNext={next} onBack={back} scope="TEACHER" />}
        {currentStep === 8 && <GenerateStep onBack={back} />}
      </div>
    </div>
  );
}
