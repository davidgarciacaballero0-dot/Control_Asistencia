# GUIA EXHAUSTIVA DE EJECUCION Y PRUEBAS QA (END-TO-END)

Este documento detalla el paso a paso para levantar todos los microservicios, el API Gateway, la base de datos PostgreSQL, el panel web React y la aplicacion movil Flutter, asi como la guia para realizar pruebas manuales y automatizadas.

---

## 1. MAPA DE PUERTOS Y SERVICIOS

| Componente | Tipo | Puerto | Base de Datos | Descripcion |
| :--- | :--- | :--- | :--- | :--- |
| **PostgreSQL 16** | BD | `5432` | `db_auth`, `db_academica`, `db_asistencia` | Servidor relacional con esquemas aislados |
| **pgAdmin 4** | GUI BD | `5050` | - | Interfaz web de administracion de BD |
| **API Gateway** | Gateway | `8080` | - | Enrutamiento central y validacion JWT |
| **MS Auth** | Backend | `8083` | `db_auth` | Autenticacion, usuarios y emision JWT |
| **MS Academico** | Backend | `8081` | `db_academica` | Docentes, Estudiantes, Materias, Grupos, Horarios, Boletas |
| **MS Asistencia** | Backend | `8082` | `db_asistencia` | Sesiones de clase, QR y OpenFeign |
| **Frontend Web** | Frontend | `3000` | - | Panel Administrativo y Proyector QR (React + Vite) |
| **App Movil** | Mobile | - | - | App de escaneo y marcacion QR (Flutter) |

---

## 2. CREDENCIALES PRECARGADAS (SEEDERS)

| Rol | Usuario / Registro | Contrasena | Nombre Completo |
| :--- | :--- | :--- | :--- |
| **ADMINISTRADOR** | `admin` | `admin123` | Administrador del Sistema |
| **DOCENTE** | `DOC-101` | `docente123` | Dr. Roberto Mendoza Ramos |
| **ESTUDIANTE 1** | `2024001` | `estudiante123` | Juan Perez Gomez |
| **ESTUDIANTE 2** | `2024002` | `estudiante123` | Maria Garcia Lopez |

---

## 3. PASO A PASO: LEVANTAMIENTO DEL ENTORNO

### Paso 3.1: Iniciar Base de Datos con Docker Compose
Desde la raiz del proyecto:
```bash
docker-compose up -d
```
*Esto creara automaticamente las tres bases de datos (`db_auth`, `db_academica`, `db_asistencia`) gracias al script `init-db/01-init.sql`.*

### Paso 3.2: Iniciar los Microservicios Backend
Abre 4 terminales independientes en la carpeta `backend/`:

**Terminal 1 (MS Auth - Puerto 8083):**
```bash
cd backend/auth-service
mvn spring-boot:run
```

**Terminal 2 (MS Academico - Puerto 8081):**
```bash
cd backend/academico-service
mvn spring-boot:run
```

**Terminal 3 (MS Asistencia - Puerto 8082):**
```bash
cd backend/asistencia-service
mvn spring-boot:run
```

**Terminal 4 (API Gateway - Puerto 8080):**
```bash
cd backend/api-gateway
mvn spring-boot:run
```

### Paso 3.3: Iniciar el Frontend Web Administrativo (React)
En una nueva terminal:
```bash
cd frontend-admin
npm install
npm run dev
```
Accede en tu navegador a: `http://localhost:3000`

### Paso 3.4: Iniciar la App Movil (Flutter)
En una nueva terminal:
```bash
cd mobile-app
flutter pub get
flutter run
```

---

## 4. GUIA DE PRUEBAS QA MANUALES (CASOS DE USO)

### Prueba 1: Autenticacion y Generacion de JWT
1. Ingresa a `http://localhost:3000`.
2. Introduce el usuario `admin` y contrasena `admin123`.
3. Verifica que la peticion viaje al Gateway (`http://localhost:8080/api/v1/auth/login`) y devuelva el token JWT.
4. El panel cargara el Dashboard con las metricas iniciales.

### Prueba 2: Gestion Academica (CU02, CU03, CU04, CU05)
1. Navega a **Docentes**: Verifica que se listan los docentes precargados. Crea un nuevo docente y editalo.
2. Navega a **Estudiantes**: Verifica que se listan los estudiantes del padron.
3. Navega a **Materias**: Observa las asignaturas registradas (ej: INF412 - Arquitectura de Software).
4. Navega a **Grupos y Horarios**: Visualiza el grupo `SC` y sus horarios (Lunes y Miercoles 07:00 a 09:15).

### Prueba 3: Inicio de Sesion de Clase y Proyeccion de QR
1. Navega a la pestana **Sesiones y QR**.
2. Presiona el boton **"Iniciar Nueva Sesion"**.
3. Selecciona el Grupo `INF412 - Grupo SC` y tema `Clase de Microservicios`.
4. Observa como se genera en pantalla el **Codigo QR interactivo** y su token unico con temporizador de expiracion.

### Prueba 4: Marcacion de Asistencia y Validacion OpenFeign (CU07)
1. En la seccion **"Simulador de Marcacion Movil"** (o directamente desde la App Flutter):
   - Ingresa el registro del estudiante inscrito: `2024001`.
   - Presiona **"Marcar Asistencia"**.
2. **Validacion esperada:**
   - El MS Asistencia invoca via **OpenFeign** al MS Academico (`/api/v1/academico/boletas/verificar`).
   - El MS Academico confirma que `2024001` esta inscrito en el grupo `SC`.
   - La asistencia se registra como **PRESENTE** en `db_asistencia`.
   - Aparece de inmediato en la tabla de **"Asistencias en Vivo"**.
3. **Prueba de Control de Duplicados:**
   - Intenta marcar nuevamente con `2024001`.
   - El sistema rechazara la peticion indicando que el estudiante ya marco asistencia en esta sesion.
4. **Prueba de Estudiante No Inscrito:**
   - Intenta marcar con el estudiante `2024003` (quien no esta inscrito en el grupo SC).
   - El sistema rechazara la marcacion indicando que el estudiante no pertenece al grupo academico.

---

## 5. EJECUCION DE PRUEBAS AUTOMATIZADAS (UNIT TESTS)

Para correr la suite de pruebas unitarias y de integracion:

```bash
cd backend
mvn test
```

Esto ejecutara las pruebas de:
- `AuthServiceTest`: Validacion de login con BCrypt, emision y parseo de JWT.
- `BoletaInscripcionServiceTest`: Verificacion cruzada de estudiantes en grupos.
- `AsistenciaServiceTest`: Validacion de tokens QR, prevencion de duplicidad y comunicacion simulada con OpenFeign.
