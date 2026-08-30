import axios from 'axios';

// URL base del API Gateway (Spring Cloud Gateway en puerto 8080)
const API_BASE_URL = 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor para inyectar el token JWT en cada peticion saliente
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor para manejar respuestas 401 (no autorizado)
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// Endpoints del Microservicio de Autenticacion (Puerto 8083 a traves de Gateway)
export const authApi = {
  login: (credentials) => apiClient.post('/api/v1/auth/login', credentials),
  register: (userData) => apiClient.post('/api/v1/auth/register', userData),
  validate: () => apiClient.get('/api/v1/auth/validate'),
  getUsers: () => apiClient.get('/api/v1/auth/users'),
};

// Endpoints del Microservicio Academico (Puerto 8081 a traves de Gateway)
export const academicoApi = {
  // Estudiantes
  getEstudiantes: (carrera) => apiClient.get('/api/v1/academico/estudiantes', { params: { carrera } }),
  getEstudiante: (registro) => apiClient.get(`/api/v1/academico/estudiantes/${registro}`),
  createEstudiante: (data) => apiClient.post('/api/v1/academico/estudiantes', data),
  updateEstudiante: (registro, data) => apiClient.put(`/api/v1/academico/estudiantes/${registro}`, data),
  deleteEstudiante: (registro) => apiClient.delete(`/api/v1/academico/estudiantes/${registro}`),

  // Docentes
  getDocentes: () => apiClient.get('/api/v1/academico/docentes'),
  getDocente: (codigo) => apiClient.get(`/api/v1/academico/docentes/${codigo}`),
  createDocente: (data) => apiClient.post('/api/v1/academico/docentes', data),
  updateDocente: (codigo, data) => apiClient.put(`/api/v1/academico/docentes/${codigo}`, data),
  deleteDocente: (codigo) => apiClient.delete(`/api/v1/academico/docentes/${codigo}`),

  // Materias
  getMaterias: () => apiClient.get('/api/v1/academico/materias'),
  getMateria: (sigla) => apiClient.get(`/api/v1/academico/materias/${sigla}`),
  createMateria: (data) => apiClient.post('/api/v1/academico/materias', data),
  updateMateria: (sigla, data) => apiClient.put(`/api/v1/academico/materias/${sigla}`, data),
  deleteMateria: (sigla) => apiClient.delete(`/api/v1/academico/materias/${sigla}`),

  // Grupos
  getGrupos: (params) => apiClient.get('/api/v1/academico/grupos', { params }),
  getGrupo: (id) => apiClient.get(`/api/v1/academico/grupos/${id}`),
  createGrupo: (data) => apiClient.post('/api/v1/academico/grupos', data),
  updateGrupo: (id, data) => apiClient.put(`/api/v1/academico/grupos/${id}`, data),
  deleteGrupo: (id) => apiClient.delete(`/api/v1/academico/grupos/${id}`),

  // Horarios
  addHorario: (grupoId, data) => apiClient.post(`/api/v1/academico/horarios/grupo/${grupoId}`, data),
  deleteHorario: (horarioId) => apiClient.delete(`/api/v1/academico/horarios/${horarioId}`),
  getHorariosByGrupo: (grupoId) => apiClient.get(`/api/v1/academico/horarios/grupo/${grupoId}`),

  // Boletas de Inscripcion y Consultas Estudiantiles
  getBoletas: () => apiClient.get('/api/v1/academico/boletas'),
  createBoleta: (data) => apiClient.post('/api/v1/academico/boletas', data),
  getMateriasInscritas: (registro) => apiClient.get(`/api/v1/academico/boletas/estudiante/${registro}/materias`),
  getClasesHoy: (registro) => apiClient.get(`/api/v1/academico/boletas/estudiante/${registro}/clases-hoy`),
  getHorariosEstudiante: (registro) => apiClient.get(`/api/v1/academico/boletas/estudiante/${registro}/horarios`),
  verificarInscripcion: (registroEstudiante, grupoId) =>
    apiClient.get('/api/v1/academico/boletas/verificar', { params: { registroEstudiante, grupoId } }),
};

// Endpoints del Microservicio de Asistencia (Puerto 8082 a traves de Gateway)
export const asistenciaApi = {
  iniciarSesion: (data) => apiClient.post('/api/v1/asistencia/sesiones/iniciar', data),
  finalizarSesion: (sesionId) => apiClient.put(`/api/v1/asistencia/sesiones/${sesionId}/finalizar`),
  regenerarQr: (sesionId, minutos) => apiClient.put(`/api/v1/asistencia/sesiones/${sesionId}/regenerar-qr`, null, { params: { minutosValidez: minutos } }),
  getSesion: (sesionId) => apiClient.get(`/api/v1/asistencia/sesiones/${sesionId}`),
  getSesionesActivas: () => apiClient.get('/api/v1/asistencia/sesiones/activas'),
  getSesionesByDocente: (codigoDocente) => apiClient.get(`/api/v1/asistencia/sesiones/docente/${codigoDocente}`),
  getSesionesByGrupo: (grupoId) => apiClient.get(`/api/v1/asistencia/sesiones/grupo/${grupoId}`),

  // Registros
  marcarAsistenciaQr: (data) => apiClient.post('/api/v1/asistencia/registros/marcar-qr', data),
  getAsistenciasBySesion: (sesionId) => apiClient.get(`/api/v1/asistencia/registros/sesion/${sesionId}`),
  getAsistenciasByEstudiante: (registro) => apiClient.get(`/api/v1/asistencia/registros/estudiante/${registro}`),
  getReporteSesion: (sesionId) => apiClient.get(`/api/v1/asistencia/registros/sesion/${sesionId}/reporte`),
};
