export interface SchoolConfig {
  id?: number;
  daysPerWeek: number;
  hoursPerDay: number;
  startTime: string;
  schoolName: string;
  academicYear: string;
}

export interface Plesso {
  id?: number;
  name: string;
  address: string;
}

export interface Subject {
  id?: number;
  name: string;
  hoursPerWeekByYear: Record<number, number>;
}

export interface SchoolClass {
  id?: number;
  year: number;
  section: string;
  plesso?: Plesso | null;
}

export interface Teacher {
  id?: number;
  firstName: string;
  lastName: string;
  subjects: Subject[];
  assignedClasses: SchoolClass[];
}

export interface TeachingAssignment {
  id?: number;
  teacher: Teacher;
  subject: Subject;
  schoolClass: SchoolClass;
  hoursPerWeek: number;
}

export type ConstraintType = 'MANDATORY' | 'PREFERRED';
export type ConstraintScope = 'TEACHER' | 'GLOBAL';
export type ConstraintCategory = 'AVAILABILITY' | 'DAY_OFF' | 'CONSECUTIVE_HOURS' | 'FIRST_LAST_HOUR' | 'MAX_HOURS_DAY' | 'NO_GAPS';

export interface TimetableConstraint {
  id?: number;
  type: ConstraintType;
  scope: ConstraintScope;
  category: ConstraintCategory;
  teacher?: Teacher | null;
  subject?: Subject | null;
  parameters: string;
  description: string;
}

export interface Timeslot {
  day: number;
  hour: number;
}

export interface Lesson {
  id: number;
  teacherId: number;
  teacherName: string;
  subjectId: number;
  subjectName: string;
  classId: number;
  className: string;
  timeslot: Timeslot | null;
}

export interface TimetableSolution {
  timeslots: Timeslot[];
  lessons: Lesson[];
  score: { hardScore: number; softScore: number } | null;
}
