import axios from 'axios';
import type {
  SchoolConfig,
  Plesso,
  Subject,
  SchoolClass,
  Teacher,
  TeachingAssignment,
  TimetableConstraint,
  TimetableSolution,
} from '../types';

const baseURL = import.meta.env.VITE_API_URL
    ? `${import.meta.env.VITE_API_URL}/api`
    : `${window.location.origin}/api`;

const api = axios.create({ baseURL });

export const configApi = {
  get: () => api.get<SchoolConfig>('/config').then(r => r.data),
  save: (config: SchoolConfig) => api.post<SchoolConfig>('/config', config).then(r => r.data),
  reset: () => api.delete('/config/reset'),
};

export const plessoApi = {
  getAll: () => api.get<Plesso[]>('/plessi').then(r => r.data),
  create: (p: Plesso) => api.post<Plesso>('/plessi', p).then(r => r.data),
  update: (id: number, p: Plesso) => api.put<Plesso>(`/plessi/${id}`, p).then(r => r.data),
  delete: (id: number) => api.delete(`/plessi/${id}`),
};

export const subjectApi = {
  getAll: () => api.get<Subject[]>('/subjects').then(r => r.data),
  create: (s: Subject) => api.post<Subject>('/subjects', s).then(r => r.data),
  update: (id: number, s: Subject) => api.put<Subject>(`/subjects/${id}`, s).then(r => r.data),
  delete: (id: number) => api.delete(`/subjects/${id}`),
};

export const classApi = {
  getAll: () => api.get<SchoolClass[]>('/classes').then(r => r.data),
  create: (c: SchoolClass) => api.post<SchoolClass>('/classes', c).then(r => r.data),
  update: (id: number, c: SchoolClass) => api.put<SchoolClass>(`/classes/${id}`, c).then(r => r.data),
  delete: (id: number) => api.delete(`/classes/${id}`),
};

export const teacherApi = {
  getAll: () => api.get<Teacher[]>('/teachers').then(r => r.data),
  create: (t: Teacher) => api.post<Teacher>('/teachers', t).then(r => r.data),
  update: (id: number, t: Teacher) => api.put<Teacher>(`/teachers/${id}`, t).then(r => r.data),
  delete: (id: number) => api.delete(`/teachers/${id}`),
};

export const assignmentApi = {
  getAll: () => api.get<TeachingAssignment[]>('/assignments').then(r => r.data),
  getByTeacher: (id: number) => api.get<TeachingAssignment[]>(`/assignments/teacher/${id}`).then(r => r.data),
  create: (a: TeachingAssignment) => api.post<TeachingAssignment>('/assignments', a).then(r => r.data),
  update: (id: number, a: TeachingAssignment) => api.put<TeachingAssignment>(`/assignments/${id}`, a).then(r => r.data),
  delete: (id: number) => api.delete(`/assignments/${id}`),
};

export const constraintApi = {
  getAll: () => api.get<TimetableConstraint[]>('/constraints').then(r => r.data),
  getGlobal: () => api.get<TimetableConstraint[]>('/constraints/global').then(r => r.data),
  getByTeacher: (id: number) => api.get<TimetableConstraint[]>(`/constraints/teacher/${id}`).then(r => r.data),
  create: (c: TimetableConstraint) => api.post<TimetableConstraint>('/constraints', c).then(r => r.data),
  update: (id: number, c: TimetableConstraint) => api.put<TimetableConstraint>(`/constraints/${id}`, c).then(r => r.data),
  delete: (id: number) => api.delete(`/constraints/${id}`),
};

export const solverApi = {
  solve: () => api.post<TimetableSolution>('/solver/solve').then(r => r.data),
  getSolution: () => api.get<TimetableSolution>('/solver/solution').then(r => r.data),
  exportExcel: () => api.get('/solver/export', { responseType: 'blob' }).then(r => {
    const url = window.URL.createObjectURL(new Blob([r.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'orario.xlsx');
    document.body.appendChild(link);
    link.click();
    link.remove();
  }),
};
