# SEGUIMIENTO DE DESARROLLO: PLAN DE IMPLEMENTACIÓN Y FASES

**Estados:**
📝 PLANIFICADO (Aún no implementado)
🚧 EN DESARROLLO
✅ FINALIZADO COMPLETAMENTE

---

## Fase 1: Infraestructura Base y Auth Service
- ✅ Configuración del Workspace (Inicialización del proyecto y estructura de módulos).
- ✅ Dockerización Base (PostgreSQL con bases de datos db_auth, db_academica, db_asistencia y pgAdmin).
- ✅ API Gateway (Puerto 8080): Configuración de rutas estáticas.
- ✅ API Gateway (Puerto 8080): Filtros globales (CORS, validación centralizada de JWT).
- ✅ MS Auth (Puerto 8083): Entidades (Usuario, Rol).
- ✅ MS Auth (Puerto 8083): Repositorios, Servicios, DTOs, Controladores y generación de JWT.
- ✅ MS Auth (Puerto 8083): Seeder de usuarios iniciales (Admin, Docente, Estudiante).

## Fase 2: MS Académico (Gestión de Datos Core)
- ✅ MS Académico (Puerto 8081): Entidades (Estudiante, Docente, Materia, Grupo, Horario, BoletaInscripcion).
- ✅ MS Académico (Puerto 8081): Repositorios Spring Data JPA.
- ✅ MS Académico (Puerto 8081): Casos de Uso / Servicios (CRUD completo CU02 a CU06).
- ✅ MS Académico (Puerto 8081): Controladores REST.
- ✅ MS Académico (Puerto 8081): Seeders (Scripts de inicialización de datos base).

## Fase 3: MS Asistencia (Control y Lógica de Negocio)
- ✅ MS Asistencia (Puerto 8082): Entidades (SesionClase, Asistencia).
- ✅ MS Asistencia (Puerto 8082): Configuración Spring Cloud OpenFeign (comunicación con MS Académico).
- ✅ MS Asistencia (Puerto 8082): Casos de Uso / Servicio de generación de sesión de clase y código QR.
- ✅ MS Asistencia (Puerto 8082): Casos de Uso / Servicio de registro de asistencia vía código QR (CU07).
- ✅ MS Asistencia (Puerto 8082): Controladores REST de sesiones y asistencias.

## Fase 4: Frontend Web y Móvil
- ✅ Frontend React.js (Admin Panel): Panel de inicio de sesión con JWT y diseño moderno.
- ✅ Frontend React.js (Admin Panel): Interfaces de gestión CRUD (docentes, estudiantes, materias, grupos, horarios).
- ✅ Frontend React.js (Admin Panel): Módulo de proyección de código QR y monitor de asistencias en tiempo real.
- ✅ Frontend Flutter (App Móvil): Inicio de sesión para estudiantes.
- ✅ Frontend Flutter (App Móvil): Vista de horarios y clases del día.
- ✅ Frontend Flutter (App Móvil): Módulo de escaneo QR y petición REST de asistencia.

## Fase 5: Pruebas QA
- ✅ Pruebas automatizadas a nivel de código (JUnit/Mockito en backend).
- ✅ Pruebas de integración de endpoints y comunicación OpenFeign.
- ✅ Guía exhaustiva de Pruebas Manuales E2E para el desarrollador.
