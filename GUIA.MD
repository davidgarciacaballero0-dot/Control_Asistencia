# GUIA MAESTRA DE ARQUITECTURA, MICROSERVICIOS Y FLUJOS DEL SISTEMA DE CONTROL DE ASISTENCIA

Esta guia ha sido elaborada para explicar de forma pedagogica, detallada y exhaustiva como esta disenado, como trabaja y que tecnologias componen el Sistema de Control de Asistencia Universitario basado en Microservicios.

---

## 1. INTRODUCCION Y VISION GENERAL DEL PROYECTO

### 1.1. El Problema que Resolvemos
En las universidades tradicionales, el control de asistencia mediante listas en papel presenta graves deficiencias:
* Perdida de tiempo util de clase mientras el docente toma asistencia nombre por nombre.
* Suplantacion de identidad (un estudiante firma por otro ausente).
* Retraso o falta de consolidacion de la informacion para reportes academicos.

### 1.2. La Solucion Desarrollada
Un ecosistema distribuido y multiplataforma compuesto por:
1. **Un Panel Web Administrativo (React + Vite):** Utilizado por administradores y docentes para gestionar el catalogo academico, iniciar sesiones de clase y proyectar un Codigo QR dinamico de alta seguridad con tiempo de expiracion.
2. **Una Aplicacion Movil (Flutter):** Utilizada por los estudiantes en sus telefonos celulares para autenticarse, consultar sus materias y escanear el Codigo QR proyectado en el aula para registrar su presencia en milisegundos.
3. **Un Backend Distribuido (Spring Boot + Spring Cloud):** Compuesto por un API Gateway y tres microservicios desacoplados que operan con sus propias bases de datos aisladas sobre PostgreSQL.

### 1.3. Por que Microservicios en lugar de un Monolito?
En una arquitectura tradicional monolitica, todo el codigo reside en un solo programa que se conecta a una unica base de datos. Si una funcionalidad falla o la base de datos se satura, todo el sistema universitario colapsa.

En nuestra arquitectura de microservicios:
* **Bases de datos independientes (Database per Service):** Ningun microservicio puede consultar o modificar directamente las tablas de otro microservicio.
* **Alta Cohesion y Bajo Acoplamiento:** Cada microservicio tiene una unica razon de cambio:
  * Si la logica de emision de tokens cambia, solo se modifica `auth-service`.
  * Si la estructura de notas o grupos cambia, solo se modifica `academico-service`.
  * Si el algoritmo de validacion de asistencia cambia, solo se modifica `asistencia-service`.
* **Comunicacion Restitucional e Interoperabilidad:** La comunicacion entre servicios se realiza a traves de contratos HTTP/REST mediante clientes declarativos OpenFeign, evitando dependencias binarias rigidas.

---

## 2. MAPA GENERAL DE ARQUITECTURA Y COMPONENTES

### 2.1. Diagrama Estructural de Componentes

```text
  +-------------------------------------------------------------------------+
  |                           CAPA DE PRESENTACION                          |
  |                                                                         |
  |   [Panel Web React + Vite]                    [App Movil Flutter]       |
  |   (Docente / Administrador)                   (Estudiante en Celular)   |
  |   http://localhost:3000                       Dispositivo Android       |
  +-----------------------------------+-------------------------------------+
                                      |
                         Peticiones HTTP REST / JSON
                                      |
                                      v
  +-------------------------------------------------------------------------+
  |                           API GATEWAY (PERIMETRO)                       |
  |                                                                         |
  |   [Spring Cloud Gateway - Puerto 8080]                                  |
  |   - Enrutamiento inteligente centralizado                               |
  |   - Filtro de Seguridad Global (AuthenticationFilter)                   |
  |   - Validacion criptografica de firmas JWT (HMAC-SHA256)                |
  +--------------------+-------------------+--------------------+-----------+
                       |                   |                    |
        /api/v1/auth/**|   /api/v1/academico/**|   /api/v1/asistencia/**
                       v                   v                    v
  +--------------------+   +---------------+   +----------------+-----------+
  |   MS AUTH          |   | MS ACADEMICO  |   |   MS ASISTENCIA            |
  |   Puerto 8083      |   | Puerto 8081   |   |   Puerto 8082              |
  |                    |   |               |   |                            |
  |   - Usuarios       |   | - Docentes    |   | - Sesiones de Clase        |
  |   - Roles          |   | - Estudiantes |   | - Generacion y Rotacion QR |
  |   - Login / BCrypt |   | - Materias    |   | - Registros de Asistencia  |
  |   - Emision JWT    |   | - Grupos      |   | - Validacion OpenFeign     |
  |                    |   | - Horarios    |   |   hacia MS Academico       |
  |                    |   | - Boletas     |   |                            |
  +---------+----------+   +-------+-------+   +------------+---------------+
            |                      |                        |
            v                      v                        v
  +---------+----------+   +-------+-------+   +------------+---------------+
  | BD db_auth         |   | BD db_academica|  | BD db_asistencia           |
  | (PostgreSQL 5433)  |   | (PostgreSQL 5433) | (PostgreSQL 5433)          |
  +--------------------+   +---------------+   +----------------------------+
```

### 2.2. Rol de Cada Componente

1. **PostgreSQL 16 (Puerto 5433):**
   * Motor de base de datos relacional ejecutado en contenedor Docker.
   * Contiene tres bases de datos separadas fisicamente: `db_auth`, `db_academica` y `db_asistencia`.
2. **API Gateway (Puerto 8080):**
   * Es la puerta de entrada unica para cualquier cliente exterior.
   * Oculta la topologia interna de la red (los clientes no necesitan saber que existen los puertos 8081, 8082 y 8083).
   * Intercepta las solicitudes, valida la firma del token JWT y rechaza con error 401 Unauthorized aquellas solicitudes no autenticadas antes de que lleguen a los servicios de negocio.
3. **MS Auth (Puerto 8083):**
   * Responsable de la gestion de identidades, almacenamiento seguro de credenciales con hashing unidireccional BCrypt y generacion de tokens JWT firmados criptograficamente.
4. **MS Academico (Puerto 8081):**
   * Corazon academico de la institucion. Administra las entidades de docentes, estudiantes, materias, grupos, horarios y la asignacion de boletas de inscripcion. Cuenta con un importador masivo de archivos Excel y CSV.
5. **MS Asistencia (Puerto 8082):**
   * Gestiona el ciclo de vida de las sesiones de clase presenciales. Emite codigos QR con tiempo limite de vigencia, procesa el escaneo del estudiante y realiza verificaciones cruzadas en tiempo real comunicandose con el MS Academico mediante OpenFeign.
6. **Frontend Web Administrativo (React - Puerto 3000):**
   * Interfaz interactiva SPA (Single Page Application) construida con React y Vite. Incluye modulo de administracion de catalogos academicos y el proyector de codigos QR para la pantalla del aula.
7. **Frontend Movil (Flutter):**
   * Aplicacion nativa para Android disenada para un escaneo rapido y comodo de los codigos QR mediante la camara integrada.

---

## 3. PRINCIPIOS DE ARQUITECTURA Y ESTANDARES DE DISENO APLICADOS

### 3.1. Clean Architecture (Arquitectura Limpia)
Cada microservicio backend esta organizado en capas estrictas con responsabilidades unicas:
1. **Capa Controller (Presentacion):** Expone endpoints REST, valida datos de entrada (`@Valid`, Jakarta Bean Validation) y define los codigos de estado HTTP (200 OK, 201 Created, 400 Bad Request, etc.).
2. **Capa Service (Casos de Uso / Logica de Negocio):** Contiene la interfaz de servicio y su implementacion (`ServiceImpl`). Aqui reside toda la logica de validaciones, calculos de tiempo de tolerancia, orquestacion y llamadas remotas.
3. **Capa Repository (Acceso a Datos / Infraestructura):** Interfaces que extienden de `JpaRepository` para la persistencia transparente sobre PostgreSQL mediante Spring Data JPA y Hibernate.
4. **Capa Domain / Entity (Dominio):** Modelado de entidades JPA que representan las tablas relacionales.
5. **Capa DTO (Data Transfer Object):** Clases inmutables para el envio y recepcion de informacion limpia, ocultando estructuras internas y contrasenas de la base de datos.

### 3.2. Principios SOLID Aplicados
* **Single Responsibility Principle (SRP):** Cada clase tiene un proposito unico. Por ejemplo, `JwtService` solo procesa y valida tokens, mientras que `AuthServiceImpl` coordina el flujo de inicio de sesion.
* **Open/Closed Principle (OCP):** Los contratos de servicios se definen mediante interfaces (`AsistenciaService`), permitiendo extender la logica sin modificar el contrato consumido por los controladores.
* **Dependency Inversion Principle (DIP):** Inyeccion de dependencias estricta a traves de constructores usando la anotacion `@RequiredArgsConstructor` de Lombok, eliminando el acoplamiento directo y facilitando la elaboracion de pruebas unitarias con Mockito.

### 3.3. Integracion Distribuida con OpenFeign
Para cumplir la regla de **Bajo Acoplamiento**, el microservicio de asistencia nunca realiza consultas SQL cruzadas a la base de datos academica. En su lugar:
1. El MS Asistencia define una interfaz declarativa `AcademicoFeignClient`.
2. Cuando se requiere verificar si un alumno pertenece a un grupo, OpenFeign serializa los parametros y emite una peticion HTTP GET a `http://localhost:8081/api/v1/academico/boletas/verificar`.
3. El MS Academico procesa la consulta en su propia base de datos `db_academica` y responde un JSON tipado.
4. Se cuenta con una clase de contingencia (`fallback`) para responder de forma controlada en caso de degradacion del servicio.

---

## 4. SECUENCIA CAPA POR CAPA DE LOS 7 CASOS DE USO (CU01 A CU07)

En esta seccion se analiza detalladamente como viaja la informacion en **cada uno de los 7 Casos de Uso**, desde el clic en la pantalla hasta la instruccion SQL en PostgreSQL, indicando exactamente que clases, metodos y DTOs intervienen.

---

### CU01: GESTIONAR USUARIO Y ROLES (Microservicio Auth)

#### Proposito
Permitir a Administradores, Docentes y Estudiantes autenticarse en el sistema y recibir un token JWT firmado criptograficamente con sus roles y reclamos de identidad.

#### Cadena de Invocacion Paso a Paso

```text
[Frontend Web / App Movil]
       |  1. axios.post('/api/v1/auth/login', LoginRequestDto)
       v
[API Gateway :8080]
       |  2. Enrutamiento publico (ignora token para /login)
       v
[AuthController :8083] (Capa Presentacion)
       |  3. login(@Valid @RequestBody LoginRequestDto request)
       v
[AuthServiceImpl] (Capa Negocio / Aplicacion)
       |  4. login(request)
       |     a) usuarioRepository.findByUsername(username)
       |     b) passwordEncoder.matches(rawPassword, encodedPassword)
       |     c) jwtService.generarToken(usuario)
       v
[UsuarioRepository] (Capa Acceso a Datos)
       |  5. findByUsername(String username)
       v
[PostgreSQL :5433 (db_auth)]
          6. SELECT * FROM usuarios WHERE username = ?
```

#### Detalle de Metodos y Componentes Involucrados:
1. **Frontend:**
   * Archivo: [LoginView.jsx](file:///d:/HDD/DOCS/docs/Universidad/ARQUITECTURA%20DE%20SOFTWARE/1ER%20EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/frontend-admin/src/views/LoginView.jsx) (o `login_view.dart` en Flutter).
   * Evento: `handleSubmit(e)` envía petición POST via Axios a `http://localhost:8080/api/v1/auth/login`.
   * Payload: `{"username": "admin", "password": "admin123"}`.
2. **API Gateway:**
   * Archivo: [application.yml](file:///d:/HDD/DOCS/docs/Universidad/ARQUITECTURA%20DE%20SOFTWARE/1ER%20EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/backend/api-gateway/src/main/resources/application.yml).
   * Regla de ruta: `Path=/api/v1/auth/**` redirige a `http://localhost:8083`.
   * En [AuthenticationFilter.java](file:///d:/HDD/DOCS/docs/Universidad/ARQUITECTURA%20DE%20SOFTWARE/1ER%20EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/backend/api-gateway/src/main/java/com/universidad/gateway/filter/AuthenticationFilter.java) se detecta que `/login` es un endpoint publico de la lista blanca (`isSecured = false`), por lo que no exige token previo.
3. **Capa Presentacion (Controller):**
   * Archivo: [AuthController.java](file:///d:/HDD/DOCS/docs/Universidad/ARQUITECTURA%20DE%20SOFTWARE/1ER%20EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/backend/auth-service/src/main/java/com/universidad/auth/controller/AuthController.java).
   * Metodo: `public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request)`.
   * DTO Entrante: `LoginRequestDto` con validaciones `@NotBlank`.
4. **Capa Negocio (Service e Implementacion):**
   * Interfaz: `AuthService.java` -> Implementacion: [AuthServiceImpl.java](file:///d:/HDD/DOCS/docs/Universidad/ARQUITECTURA%20DE%20SOFTWARE/1ER%20EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/backend/auth-service/src/main/java/com/universidad/auth/service/impl/AuthServiceImpl.java).
   * Metodo: `public LoginResponseDto login(LoginRequestDto request)`.
   * Operaciones:
     * Llama a `usuarioRepository.findByUsername(request.getUsername())`. Si no existe, lanza `BadCredentialsException`.
     * Invoca a `passwordEncoder.matches(request.getPassword(), usuario.getPassword())` usando BCrypt.
     * Invoca a `jwtService.generarToken(usuario)` que embebe claims: `sub`, `roles`, `nombreCompleto`, `email`, `ci`, `identificadorReferencia`.
5. **Capa Datos (Repository) y Base de Datos:**
   * Archivo: `UsuarioRepository.java` extiende `JpaRepository<Usuario, Long>`.
   * SQL ejecutado en PostgreSQL (`db_auth`):
     ```sql
     SELECT u.*, r.nombre FROM usuarios u LEFT JOIN usuario_roles ur ON u.id = ur.usuario_id LEFT JOIN roles r ON ur.rol_id = r.id WHERE u.username = 'admin';
     ```
6. **Ruta de Retorno:**
   * Se construye el DTO de respuesta `LoginResponseDto.builder().token(jwt).tipo("Bearer").roles(...).build()`.
   * El controlador responde `200 OK`.
   * El frontend guarda el token en `localStorage` (Web) o `SharedPreferences` (Movil) para adjuntarlo en todas las peticiones posteriores.

---

### CU02: GESTIONAR DOCENTE (Microservicio Academico)

#### Proposito
Registrar, listar, modificar y dar de baja a los profesores universitarios que dictan asignaturas.

#### Cadena de Invocacion Paso a Paso

```text
[Frontend Web :3000]
       |  1. axios.post('/api/v1/academico/docentes', DocenteDto, { headers: Bearer Token })
       v
[API Gateway :8080]
       |  2. AuthenticationFilter valida firma del JWT con la clave secreta
       |  3. Redirige a http://localhost:8081/api/v1/academico/docentes
       v
[DocenteController :8081] (Capa Presentacion)
       |  4. crear(@Valid @RequestBody DocenteDto dto)
       v
[DocenteServiceImpl] (Capa Negocio / Aplicacion)
       |  5. crear(DocenteDto dto)
       |     a) Valida if (docenteRepository.existsByCodigo(dto.getCodigo()))
       |     b) Docente.builder().codigo(...).nombres(...)...build()
       |     c) docenteRepository.save(docente)
       |     d) Mapea Entidad -> DocenteDto
       v
[DocenteRepository] (Capa Acceso a Datos)
       |  6. save(Docente entity)
       v
[PostgreSQL :5433 (db_academica)]
          7. INSERT INTO docentes (codigo, nombres, apellidos, correo, telefono, especialidad) VALUES (...)
```

#### Detalle de Metodos y Componentes Involucrados:
1. **Frontend:**
   * Archivo: `DocentesView.jsx`.
   * Evento: El Administrador completa el formulario y presiona "Guardar Docente".
   * Llamada: `axios.post('http://localhost:8080/api/v1/academico/docentes', docenteForm)`.
   * DTO Enviado: `{"codigo": "DOC-101", "nombres": "Roberto", "apellidos": "Mendoza Ramos", "correo": "roberto.mendoza@universidad.edu", "telefono": "70012345", "especialidad": "Ingenieria de Software"}`.
2. **API Gateway:**
   * Intercepta la peticion.
   * `AuthenticationFilter` extrae el token Bearer, verifica que no este expirado y comprueba que la firma HMAC-SHA256 corresponda a la `jwt.secret`.
   * Pasa la peticion intacta a `academico-service` (puerto 8081).
3. **Capa Presentacion (Controller):**
   * Archivo: [DocenteController.java](file:///d:/HDD/DOCS/docs/Universidad/ARQUITECTURA%20DE%20SOFTWARE/1ER%20EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/backend/academico-service/src/main/java/com/universidad/academico/controller/DocenteController.java).
   * Metodo: `public ResponseEntity<DocenteDto> crear(@Valid @RequestBody DocenteDto dto)`.
4. **Capa Negocio (Service e Implementacion):**
   * Archivo: `DocenteServiceImpl.java`.
   * Metodo: `public DocenteDto crear(DocenteDto dto)`.
   * Validacion de regla de negocio: Comprueba si el codigo ya esta registrado. Si ya existe, lanza `IllegalArgumentException("Ya existe un docente con el codigo...")`.
   * Construccion de entidad: `Docente docente = mapearAEntidad(dto)`.
5. **Capa Datos (Repository) y Base de Datos:**
   * Archivo: `DocenteRepository.java` extiende `JpaRepository<Docente, String>`.
   * Metodo: `docenteRepository.save(docente)`.
   * SQL ejecutado en PostgreSQL (`db_academica`):
     ```sql
     INSERT INTO docentes (codigo, nombres, apellidos, correo, telefono, especialidad) 
     VALUES ('DOC-101', 'Roberto', 'Mendoza Ramos', 'roberto.mendoza@universidad.edu', '70012345', 'Ingenieria de Software');
     ```
6. **Ruta de Retorno:**
   * `DocenteServiceImpl` convierte la entidad persistida a `DocenteDto`.
   * El controlador responde con estado `201 CREATED`.
   * En el frontend, el estado `docentes` de React se actualiza inmediatamente (`setDocentes([...docentes, nuevoDocente])`) y la tabla se actualiza sin recargar la pagina.

---

### CU03: GESTIONAR ESTUDIANTE (Microservicio Academico)

#### Proposito
Mantener el padron oficial de alumnos universitarios identificados por su numero de registro unico.

#### Cadena de Invocacion Paso a Paso

```text
[Frontend Web :3000]
       |  1. axios.post('/api/v1/academico/estudiantes', EstudianteDto)
       v
[API Gateway :8080]
       |  2. Valida JWT y enruta a :8081
       v
[EstudianteController :8081] (Capa Presentacion)
       |  3. crear(@Valid @RequestBody EstudianteDto dto)
       v
[EstudianteServiceImpl] (Capa Negocio / Aplicacion)
       |  4. crear(EstudianteDto dto)
       |     a) Valida existsById(dto.getRegistro())
       |     b) Estudiante.builder().registro(...)...build()
       |     c) estudianteRepository.save(estudiante)
       v
[EstudianteRepository] (Capa Acceso a Datos)
       |  5. save(Estudiante entity)
       v
[PostgreSQL :5433 (db_academica)]
          6. INSERT INTO estudiantes (registro, nombres, apellidos, correo, telefono, carrera, plan_estudio, foto_base64) VALUES (...)
```

#### Detalle de Metodos y Componentes Involucrados:
1. **Frontend:**
   * Archivo: `EstudiantesView.jsx`.
   * Envia el DTO con datos del estudiante (registro, nombre, apellidos, carrera, plan, correo, etc.).
2. **API Gateway:**
   * Valida autorizacion mediante `AuthenticationFilter` y reenvia a `http://localhost:8081/api/v1/academico/estudiantes`.
3. **Capa Presentacion (Controller):**
   * Archivo: `EstudianteController.java`.
   * Metodo: `public ResponseEntity<EstudianteDto> crear(@Valid @RequestBody EstudianteDto dto)`.
4. **Capa Negocio (Service e Implementacion):**
   * Archivo: `EstudianteServiceImpl.java`.
   * Metodo: `public EstudianteDto crear(EstudianteDto dto)`.
   * Regla de Negocio: Verifica que el registro numerico (ej: `2024001`) no este duplicado.
5. **Capa Datos (Repository) y Base de Datos:**
   * Archivo: `EstudianteRepository.java` extiende `JpaRepository<Estudiante, String>`.
   * SQL en PostgreSQL (`db_academica`):
     ```sql
     INSERT INTO estudiantes (registro, nombres, apellidos, correo, telefono, carrera, plan_estudio, foto_base64)
     VALUES ('2024001', 'Juan', 'Perez Gomez', 'juan.perez@universidad.edu', '71234567', 'Ingenieria Informatica', '187-4', 'data:image/jpeg;base64,...');
     ```
6. **Ruta de Retorno:**
   * Se mapea a `EstudianteDto` y el controlador retorna `201 CREATED`. El frontend anade el nuevo estudiante a la lista activa.

---

### CU04: GESTIONAR MATERIA (Microservicio Academico)

#### Proposito
Administrar las materias de la malla curricular universitaria (por ejemplo: `INF412` - Arquitectura de Software).

#### Cadena de Invocacion Paso a Paso

```text
[Frontend Web :3000]
       |  1. axios.post('/api/v1/academico/materias', MateriaDto)
       v
[API Gateway :8080]
       |  2. Valida JWT y enruta a :8081
       v
[MateriaController :8081] (Capa Presentacion)
       |  3. crear(@Valid @RequestBody MateriaDto dto)
       v
[MateriaServiceImpl] (Capa Negocio / Aplicacion)
       |  4. crear(MateriaDto dto)
       |     a) Valida existsById(dto.getSigla())
       |     b) Materia.builder().sigla(...)...build()
       |     c) materiaRepository.save(materia)
       v
[MateriaRepository] (Capa Acceso a Datos)
       |  5. save(Materia entity)
       v
[PostgreSQL :5433 (db_academica)]
          6. INSERT INTO materias (sigla, nombre, creditos, nivel_semestre, carrera) VALUES (...)
```

#### Detalle de Metodos y Componentes Involucrados:
1. **Frontend:** `MateriasView.jsx` envia peticion POST a `/api/v1/academico/materias`.
2. **API Gateway:** Enruta tras validar el JWT Bearer hacia el puerto 8081.
3. **Capa Presentacion (Controller):** `MateriaController.java` recibe `MateriaDto`.
4. **Capa Negocio (Service e Implementacion):** `MateriaServiceImpl.java` valida que la sigla sea unica y construye la entidad JPA.
5. **Capa Datos (Repository) y Base de Datos:** `MateriaRepository.java` ejecuta el `save()` en la tabla `materias` de `db_academica`.
6. **Ruta de Retorno:** Devuelve el `MateriaDto` creado con codigo `201 CREATED`.

---

### CU05: GESTIONAR GRUPO Y HORARIOS (Microservicio Academico)

#### Proposito
Crear comisiones de materias asignando el docente responsable y los horarios semanales de clase (dia, hora de inicio, hora de fin y aula).

#### Cadena de Invocacion Paso a Paso

```text
[Frontend Web :3000]
       |  1. axios.post('/api/v1/academico/grupos', GrupoDto)
       v
[API Gateway :8080]
       |  2. Valida JWT y enruta a :8081
       v
[GrupoController :8081] (Capa Presentacion)
       |  3. crear(@Valid @RequestBody GrupoDto dto)
       v
[GrupoServiceImpl] (Capa Negocio / Aplicacion)
       |  4. crear(GrupoDto dto)
       |     a) materiaRepository.findById(dto.getSiglaMateria())
       |     b) docenteRepository.findById(dto.getCodigoDocente())
       |     c) Construye Grupo con sus relaciones JPA
       |     d) grupoRepository.save(grupo)
       v
[GrupoRepository & HorarioRepository] (Capa Acceso a Datos)
       |  5. Persistencia relacional de Grupo y Horarios
       v
[PostgreSQL :5433 (db_academica)]
          6. INSERT INTO grupos (nombre_grupo, cupo_maximo, materia_sigla, docente_codigo) VALUES (...)
             INSERT INTO horarios (dia_semana, hora_inicio, hora_fin, aula, grupo_id) VALUES (...)
```

#### Detalle de Metodos y Componentes Involucrados:
1. **Frontend:**
   * Archivo: `GruposView.jsx`.
   * Envia: `{"nombreGrupo": "SC", "cupoMaximo": 40, "siglaMateria": "INF412", "codigoDocente": "DOC-101", "horarios": [{"diaSemana": "LUNES", "horaInicio": "07:00", "horaFin": "09:15", "aula": "236-12"}]}`.
2. **API Gateway:**
   * Enruta a `http://localhost:8081/api/v1/academico/grupos`.
3. **Capa Presentacion (Controller):**
   * Archivo: [GrupoController.java](file:///d:/HDD/DOCS/docs/Universidad/ARQUITECTURA%20DE%20SOFTWARE/1ER%20EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/backend/academico-service/src/main/java/com/universidad/academico/controller/GrupoController.java).
   * Metodo: `public ResponseEntity<GrupoDto> crear(@Valid @RequestBody GrupoDto dto)`.
4. **Capa Negocio (Service e Implementacion):**
   * Archivo: `GrupoServiceImpl.java`.
   * Metodo: `public GrupoDto crear(GrupoDto dto)`.
   * Logica:
     * Busca la `Materia` por sigla y el `Docente` por codigo. Si no existen, arroja excepcion.
     * Construye la entidad `Grupo`.
     * Itera los horarios proporcionados, asignandoles la referencia bidireccional hacia el grupo.
5. **Capa Datos (Repository) y Base de Datos:**
   * `GrupoRepository.java` y `HorarioRepository.java` guardan las entidades en las tablas `grupos` y `horarios` de `db_academica`.
6. **Ruta de Retorno:**
   * Retorna `GrupoDto` con sus horarios anidados y el ID asignado por la secuencia de la base de datos.

---

### CU06: GESTIONAR BOLETA DE INSCRIPCION (Microservicio Academico)

#### Proposito
Inscribir formalmente a los estudiantes en sus grupos academicos correspondientes y proveer el endpoint de verificacion interna consumido por OpenFeign.

#### Cadena de Invocacion Paso a Paso (Inscripcion Regular o Masiva)

```text
[Frontend Web :3000]
       |  1. axios.post('/api/v1/academico/boletas', BoletaInscripcionDto)
       v
[API Gateway :8080]
       |  2. Valida JWT y enruta a :8081
       v
[BoletaInscripcionController :8081] (Capa Presentacion)
       |  3. inscribirEstudiante(@Valid @RequestBody BoletaInscripcionDto dto)
       v
[BoletaInscripcionServiceImpl] (Capa Negocio / Aplicacion)
       |  4. inscribirEstudiante(dto)
       |     a) estudianteRepository.findById(dto.getRegistroEstudiante())
       |     b) grupoRepository.findById(grupoId)
       |     c) Valida cupo disponible del grupo
       |     d) boletaRepository.save(boleta)
       v
[BoletaInscripcionRepository] (Capa Acceso a Datos)
       |  5. Persiste boleta_inscripcion y boleta_grupos
       v
[PostgreSQL :5433 (db_academica)]
          6. INSERT INTO boletas_inscripcion (fecha, gestion, estudiante_registro) VALUES (...)
             INSERT INTO boletas_grupos (boleta_numero, grupo_id) VALUES (...)
```

#### El Endpoint Clave para OpenFeign:
Adicionalmente, este controlador expone el metodo fundamental que utiliza el microservicio de Asistencia:
* **Controlador:** [BoletaInscripcionController.java](file:///d:/HDD/DOCS/docs/Universidad/ARQUITECTURA%20DE%20SOFTWARE/1ER%20EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/backend/academico-service/src/main/java/com/universidad/academico/controller/BoletaInscripcionController.java).
* **Metodo:**
  ```java
  @GetMapping("/verificar")
  public ResponseEntity<VerificacionInscripcionDto> verificarInscripcion(
          @RequestParam String registroEstudiante,
          @RequestParam Long grupoId
  )
  ```
* **Llamada interna en Servicio:** En [BoletaInscripcionServiceImpl.java](file:///d:/HDD/DOCS/docs/Universidad/ARQUITECTURA%20DE%20SOFTWARE/1ER%20EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/backend/academico-service/src/main/java/com/universidad/academico/service/impl/BoletaInscripcionServiceImpl.java#L98-L125), ejecuta:
  `boletaRepository.existsByEstudianteRegistroAndGrupoId(registroEstudiante, grupoId)`
* **Respuesta:** Devuelve `VerificacionInscripcionDto` con `inscrito = true/false` y los nombres de la materia y estudiante.

---

### CU07: GESTIONAR ASISTENCIA (Microservicio Asistencia e Interoperabilidad OpenFeign)

#### Proposito
Gestionar el ciclo completo de la sesion presencial de clase, la emision y proyeccion del QR dinamico, el escaneo desde el movil y la verificacion cruzada con el microservicio academico sin compartir bases de datos.

Este caso de uso comprende **dos flujos encadenados**:
1. **Sub-flujo A:** El Docente inicia la sesion y genera el Codigo QR.
2. **Sub-flujo B:** El Estudiante escanea el QR desde su movil y se registra la asistencia.

---

#### Sub-flujo A: Apertura de Sesion y Generacion del Token QR (Docente)

```text
[Frontend Web (Docente)]
       |  1. axios.post('/api/v1/asistencia/sesiones/iniciar', IniciarSesionDto)
       v
[API Gateway :8080]
       |  2. Valida JWT de Docente y enruta a :8082
       v
[SesionClaseController :8082] (Capa Presentacion)
       |  3. iniciarSesion(@Valid @RequestBody IniciarSesionDto dto)
       v
[SesionClaseServiceImpl] (Capa Negocio / Aplicacion)
       |  4. iniciarSesion(dto)
       |     a) Auto-finaliza sesiones anteriores abiertas del docente
       |     b) Genera token QR criptografico: "QR-" + UUID
       |     c) Calcula expiracion: LocalDateTime.now().plusMinutes(15)
       |     d) Construye entidad SesionClase con estado ACTIVA
       |     e) sesionRepository.save(sesion)
       v
[SesionClaseRepository] (Capa Acceso a Datos)
       |  5. save(SesionClase entity)
       v
[PostgreSQL :5433 (db_asistencia)]
          6. INSERT INTO sesiones_clase (fecha, hora_inicio, estado, codigo_qr_generado, expiracion_qr, id_grupo_referencia, codigo_docente_referencia, tema) VALUES (...)
```

* **Respuesta hacia la Web:** Devuelve `SesionClaseDto` conteniendo `codigoQrGenerado = "QR-E7F20B8C54A1893D"`.
* **Proyeccion:** El panel React toma ese valor y utiliza `qrcode.react` para dibujar el codigo QR en pantalla gigante en el aula con su contador de cuenta regresiva.

---

#### Sub-flujo B: Marcado Movil, Validacion en Cascada y Llamada OpenFeign (Estudiante)

```text
[App Movil Flutter (Estudiante)]
       |  1. Camara escanea "QR-E7F20B8C54A1893D"
       |  2. http.post('/api/v1/asistencia/registros/marcar-qr', MarcarAsistenciaQrDto)
       v
[API Gateway :8080]
       |  3. AuthenticationFilter valida JWT del Estudiante
       |  4. Enruta a http://localhost:8082/api/v1/asistencia/registros/marcar-qr
       v
[AsistenciaController :8082] (Capa Presentacion Asistencia)
       |  5. registrarAsistenciaQr(@Valid @RequestBody MarcarAsistenciaQrDto dto)
       v
[AsistenciaServiceImpl] (Capa Negocio Asistencia)
       |  6. registrarAsistenciaQr(dto):
       |     Filtro 1: sesionRepository.findByCodigoQrGenerado(dto.getCodigoQr())
       |     Filtro 2: Valida sesion.getEstado() == ACTIVA y now() < expiracionQr
       |     Filtro 3: Valida !asistenciaRepository.existsBySesionClaseIdAndRegistroEstudiante(...)
       |     Filtro 4: Llamada OpenFeign cruzada hacia MS Academico
       |               academicoClient.verificarInscripcion(registro, idGrupoReferencia)
       |               -------------------------------------------------------------> [MS Academico :8081]
       |                                                                              GET /api/v1/academico/boletas/verificar
       |                                                                              Busca en db_academica:
       |                                                                              boletaRepository.existsBy...
       |               <------------------------------------------------------------- Responde 200 OK (inscrito: true)
       |     Filtro 5: Determina puntualidad (<= 15 min ? PRESENTE : ATRASO)
       |     Crea entidad Asistencia y guarda en db_asistencia
       v
[AsistenciaRepository] (Capa Acceso a Datos)
       |  7. save(Asistencia entity)
       v
[PostgreSQL :5433 (db_asistencia)]
          8. INSERT INTO asistencias (fecha_registro, hora_registro, estado, metodo_validacion, registro_estudiante, sesion_clase_id) VALUES (...)
```

* **Respuesta hacia el Movil:** Devuelve `AsistenciaResponseDto` con codigo `201 CREATED`. La app Flutter muestra una alerta emergente de confirmacion.
* **Refresco en el Aula:** El panel web React del docente, que consulta periodicamente `/api/v1/asistencia/registros/sesion/{id}`, recibe el nuevo registro y muestra al estudiante en la lista de alumnos presentes en vivo.

---

## 5. RESUMEN DE FLUJO DE CAPAS: TABLA DE REFERENCIA RAPIDA

Para cualquier caso de uso en esta arquitectura, el flujo de llamadas sigue invariablemente este orden:

| Orden | Capa / Elemento | Tecnologia / Clase | Responsabilidad Exacta |
| :--- | :--- | :--- | :--- |
| **1** | **Interfaz de Usuario** | React (Web) o Flutter (Movil) | Captura eventos del usuario, valida formularios y construye el JSON de envio. |
| **2** | **Transporte HTTP** | Axios / `http` de Dart | Envia la peticion con la cabecera `Authorization: Bearer <JWT>`. |
| **3** | **Gateway Perimetral** | Spring Cloud Gateway (8080) | Enruta segun el prefijo de URL (`/auth`, `/academico`, `/asistencia`) y valida el token. |
| **4** | **Presentacion (Controller)** | `@RestController` en Spring Boot | Deserializa el JSON a DTO, ejecuta `@Valid` y llama a la interfaz de servicio. |
| **5** | **Aplicacion / Negocio (Service)** | `@Service` `*ServiceImpl` | Aplica reglas de negocio, validaciones cruzadas, invoca OpenFeign si aplica y mapea a Entidad. |
| **6** | **Persistencia (Repository)** | `JpaRepository` en Spring Data | Traduce metodos Java a instrucciones SQL mediante Hibernate. |
| **7** | **Motor de Base de Datos** | PostgreSQL 16 (Docker 5433) | Inserta o consulta en la base de datos aislada del microservicio correspondiente. |
| **8** | **Retorno** | DTO de Salida -> HTTP Status | Transforma la entidad en DTO de respuesta y devuelve `200 OK` o `201 CREATED`. |

---

## 6. GUIA PARA DEFENDER EL PROYECTO ANTE EL TRIBUNAL O DOCENTE EVALUADOR

Preguntas clasicas de arquitectura de software y como responderlas con solidez tecnica:

### Pregunta 1: Por que decidieron usar Microservicios y no una aplicacion monolitica tradicional?
* **Respuesta modelo:**
  > *"Elegimos una arquitectura de microservicios para garantizar Alta Cohesion, Bajo Acoplamiento y Escalabilidad Independiente. Cada dominio (Autenticacion, Catalogo Academico y Control de Asistencia) maneja su propio ciclo de vida y su propia base de datos (Database per Service). Si en un dia de inscripciones el servicio academico recibe una carga masiva de transacciones, no afecta la disponibilidad del servicio de autenticacion ni de marcado de asistencia en las aulas. Ademas, permite que diferentes equipos trabajen en distintas partes del sistema sin riesgo de colisiones en el codigo."*

### Pregunta 2: Como garantizan la consistencia de datos si las bases de datos estan separadas y no pueden usar JOINs de SQL?
* **Respuesta modelo:**
  > *"Respetamos de forma estricta el principio de que un microservicio no debe conocer la estructura interna de la base de datos de otro. Para validar que un estudiante pertenezca a una materia, utilizamos un cliente declarativo OpenFeign. El microservicio de asistencia emite una peticion HTTP REST al microservicio academico pasandole el registro y el ID del grupo. Es el microservicio academico quien revisa su propia base de datos `db_academica` y devuelve un DTO con la confirmacion. De esta forma, desacoplamos la persistencia fisica pero mantenemos la integridad logica a traves de contratos de API bien definidos."*

### Pregunta 3: Cual es el proposito del API Gateway y como maneja la seguridad?
* **Respuesta modelo:**
  > *"El API Gateway (Spring Cloud Gateway) actua como punto perimetral unico. Centraliza la solucion del problema de CORS y aplica un filtro de seguridad global (`AuthenticationFilter`). Cuando un cliente envia una solicitud con una cabecera `Authorization: Bearer <token>`, el Gateway verifica la firma criptografica del JWT mediante la clave secreta compartida sin necesidad de consultar ninguna base de datos. Si el token es legitimo y no ha expirado, enruta la peticion al microservicio interno; si es invalido o viene vacio, rechaza la peticion inmediatamente con un codigo 401 Unauthorized, protegiendo a los microservicios internos de trafico no deseado."*

### Pregunta 4: Que patrones de diseno de software aplicaron en el codigo?
* **Respuesta modelo:**
  > *"En el backend aplicamos:
  > 1. **Inversion de Dependencias (DIP)** e Inyeccion de Dependencias en constructores con Lombok (`@RequiredArgsConstructor`).
  > 2. **Data Transfer Object (DTO)** para desacoplar el modelo de base de datos de la capa de transporte.
  > 3. **Declarative REST Client (OpenFeign)** para la comunicacion entre microservicios.
  > 4. **Circuit Breaker / Fallback Pattern** para tolerancia a fallos en la comunicacion remota.
  > 5. **Builder Pattern** para la instanciacion inmutable y segura de entidades y DTOs.
  > En el frontend aplicamos el patron **Single Page Application (SPA)** con React y una arquitectura por capas en Flutter dividida en Views, Models y Services."*

---

## 7. MAPA DE ARCHIVOS Y CODIGO CLAVE PARA MOSTRAR EN LA PRESENTACION

Si el docente pide *"Muestrame el codigo donde se hace X cosa"*, estos son los archivos clave para abrir directamente:

1. **La llamada entre microservicios (OpenFeign):**
   * Archivo: [AcademicoFeignClient.java](file:///d:/HDD/DOCS/docs/Universidad/ARQUITECTURA%20DE%20SOFTWARE/1ER%20EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/backend/asistencia-service/src/main/java/com/universidad/asistencia/client/AcademicoFeignClient.java)
2. **La logica central de validacion y marcado de asistencia (CU07):**
   * Archivo: [AsistenciaServiceImpl.java](file:///d:/HDD/DOCS/docs/Universidad/ARQUITECTURA%20DE%20SOFTWARE/1ER%20EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/backend/asistencia-service/src/main/java/com/universidad/asistencia/service/impl/AsistenciaServiceImpl.java)
3. **El inicio de sesion y generacion del token QR temporal:**
   * Archivo: [SesionClaseServiceImpl.java](file:///d:/HDD/DOCS/docs/Universidad/ARQUITECTURA%20DE%20SOFTWARE/1ER%20EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/backend/asistencia-service/src/main/java/com/universidad/asistencia/service/impl/SesionClaseServiceImpl.java)
4. **El enrutamiento y filtro de seguridad en el Gateway:**
   * Archivo: [application.yml del Gateway](file:///d:/HDD/DOCS/docs/Universidad/ARQUITECTURA%20DE%20SOFTWARE/1ER%20EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/backend/api-gateway/src/main/resources/application.yml)
   * Archivo: [AuthenticationFilter.java](file:///d:/HDD/DOCS/docs/Universidad/ARQUITECTURA%20DE%20SOFTWARE/1ER%20EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/backend/api-gateway/src/main/java/com/universidad/gateway/filter/AuthenticationFilter.java)
5. **El escaneo de camara en la aplicacion movil:**
   * Archivo: [qr_scanner_view.dart](file:///d:/HDD/DOCS/docs/Universidad/ARQUITECTURA%20DE%20SOFTWARE/1ER%20EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/mobile-app/lib/views/qr_scanner_view.dart)
6. **La proyeccion interactiva del QR en el panel web:**
   * Archivo: [SesionesView.jsx](file:///d:/HDD/DOCS/docs/Universidad/ARQUITECTURA%20DE%20SOFTWARE/1ER%20EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/frontend-admin/src/views/SesionesView.jsx)
7. **La definicion de contenedores y bases de datos aisladas:**
   * Archivo: [docker-compose.yml](file:///d:/HDD/DOCS/docs/Universidad/ARQUITECTURA%20DE%20SOFTWARE/1ER%20EXAMEN/CONTROL_ASISTENCIA_MICROSERVICIOS/docker-compose.yml)
