import { useState } from 'react';
import type { TimetableSolution } from '../../types';

interface Props {
  solution: TimetableSolution;
}

const DAY_NAMES = ['Lunedì', 'Martedì', 'Mercoledì', 'Giovedì', 'Venerdì', 'Sabato'];

export default function TimetableView({ solution }: Props) {
  const [viewBy, setViewBy] = useState<'teacher' | 'class'>('teacher');

  const maxDay = Math.max(...solution.timeslots.map(t => t.day));
  const maxHour = Math.max(...solution.timeslots.map(t => t.hour));

  const lessonsWithSlot = solution.lessons.filter(l => l.timeslot);

  const groupedByTeacher = groupBy(lessonsWithSlot, l => l.teacherName);
  const groupedByClass = groupBy(lessonsWithSlot, l => l.className);

  const grouped = viewBy === 'teacher' ? groupedByTeacher : groupedByClass;

  return (
    <div>
      <div style={{ marginBottom: '1rem', textAlign: 'center' }}>
        <button
          className={`btn ${viewBy === 'teacher' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setViewBy('teacher')}
          style={{ marginRight: '0.5rem' }}
        >
          Per Docente
        </button>
        <button
          className={`btn ${viewBy === 'class' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setViewBy('class')}
        >
          Per Classe
        </button>
      </div>

      <div className="timetable-grid">
        <table>
          <thead>
            <tr>
              <th>{viewBy === 'teacher' ? 'Docente' : 'Classe'}</th>
              {Array.from({ length: maxDay + 1 }, (_, day) =>
                Array.from({ length: maxHour + 1 }, (_, hour) => (
                  <th key={`${day}-${hour}`}>
                    {DAY_NAMES[day]}<br />{hour + 1}ª
                  </th>
                ))
              ).flat()}
            </tr>
          </thead>
          <tbody>
            {Object.entries(grouped)
              .sort(([a], [b]) => a.localeCompare(b))
              .map(([key, lessons]) => (
                <tr key={key}>
                  <td><strong>{key}</strong></td>
                  {Array.from({ length: maxDay + 1 }, (_, day) =>
                    Array.from({ length: maxHour + 1 }, (_, hour) => {
                      const lesson = lessons.find(
                        l => l.timeslot?.day === day && l.timeslot?.hour === hour
                      );
                      return (
                        <td key={`${day}-${hour}`} className={lesson ? 'filled' : ''}>
                          {lesson && (
                            viewBy === 'teacher'
                              ? `${lesson.subjectName} (${lesson.className})`
                              : `${lesson.teacherName} - ${lesson.subjectName}`
                          )}
                        </td>
                      );
                    })
                  ).flat()}
                </tr>
              ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function groupBy<T>(arr: T[], key: (item: T) => string): Record<string, T[]> {
  return arr.reduce((acc, item) => {
    const k = key(item);
    (acc[k] = acc[k] || []).push(item);
    return acc;
  }, {} as Record<string, T[]>);
}
