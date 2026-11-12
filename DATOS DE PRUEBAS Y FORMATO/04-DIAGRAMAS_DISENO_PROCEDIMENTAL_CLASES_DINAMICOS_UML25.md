# DOCUMENTACION DE DISENO PROCEDIMENTAL Y DIAGRAMAS DE CLASES DINAMICOS (ESTANDAR OMG UML 2.5+)

Este documento especifica los **Diagramas de Diseno Procedimental (Diagramas de Clases Dinamicos, Diagramas de Secuencia de Diseno y Diagramas de Comunicacion)** bajo el estandar **OMG UML 2.5+** para los dos patrones de diseno implementados en el proyecto:
1. **Patron 1: Chain of Responsibility** en `CU07 - GESTIONAR ASISTENCIA` (`asistencia-service`).
2. **Patron 2: Strategy** en `CU03 - GESTIONAR ESTUDIANTE` (`academico-service`).

---

## 1. CU07 - GESTIONAR ASISTENCIA: Patron Chain of Responsibility (UML 2.5+)

### 1.1. Diagrama de Clases Dinamico / Estructural de Diseno (UML 2.5+)

```mermaid
classDiagram
    direction TB

    class AsistenciaController {
        <<controller>>
        -asistenciaService : AsistenciaService
        +marcarAsistenciaQr(dto : MarcarAsistenciaQrDto) : ResponseEntity~AsistenciaResponseDto~
    }

    class AsistenciaService {
        <<interface>>
        +registrarAsistenciaQr(dto : MarcarAsistenciaQrDto) : AsistenciaResponseDto
        +listarPorSesion(sesionId : Long) : List~AsistenciaResponseDto~
        +listarPorEstudiante(registroEstudiante : String) : List~AsistenciaResponseDto~
        +generarReporteSesion(sesionId : Long) : ReporteAsistenciaDto
    }

    class AsistenciaServiceImpl {
        <<Client>>
        -asistenciaRepository : AsistenciaRepository
        -sesionRepository : SesionClaseRepository
        -academicoClient : AcademicoFeignClient
        -abstractHandler : AbstractAsistenciaHandler
        +configurarCadenaResponsabilidad() : void
        +registrarAsistenciaQr(dto : MarcarAsistenciaQrDto) : AsistenciaResponseDto
    }

    class AbstractAsistenciaHandler {
        <<abstract>>
        #siguiente : AbstractAsistenciaHandler
        +setSiguiente(siguiente : AbstractAsistenciaHandler) : AbstractAsistenciaHandler
        +getSiguiente() : AbstractAsistenciaHandler
        +handleRequest(request : AsistenciaValidationContext)* : void
        #pasarAlSiguiente(request : AsistenciaValidationContext) : void
    }

    class SesionActivaHandler {
        <<ConcreteHandler1>>
        -sesionRepository : SesionClaseRepository
        +handleRequest(request : AsistenciaValidationContext) : void
    }

    class VigenciaQrHandler {
        <<ConcreteHandler2>>
        +handleRequest(request : AsistenciaValidationContext) : void
    }

    class DuplicidadAsistenciaHandler {
        <<ConcreteHandler3>>
        -asistenciaRepository : AsistenciaRepository
        +handleRequest(request : AsistenciaValidationContext) : void
    }

    class InscripcionAcademicaHandler {
        <<ConcreteHandler4>>
        -academicoClient : AcademicoFeignClient
        +handleRequest(request : AsistenciaValidationContext) : void
    }

    class CalculoPuntualidadHandler {
        <<ConcreteHandler5>>
        +TOLERANCIA_MINUTOS_POR_DEFECTO : int = 15$
        -toleranciaMinutos : int
        +handleRequest(request : AsistenciaValidationContext) : void
    }

    class AsistenciaValidationContext {
        <<Request>>
        -registroEstudiante : String
        -codigoQr : String
        -observacion : String
        -fechaEvaluacion : LocalDate
        -horaEvaluacion : LocalTime
        -sesionClase : SesionClase
        -verificacionInscripcion : VerificacionInscripcionResponse
        -estadoAsistenciaCalculado : EstadoAsistencia
    }

    AsistenciaController --> AsistenciaService : invoca
    AsistenciaService <|.. AsistenciaServiceImpl : realiza
    AsistenciaServiceImpl --> AbstractAsistenciaHandler : -abstractHandler (1)
    AbstractAsistenciaHandler o--> AbstractAsistenciaHandler : #siguiente (0..1)
    AbstractAsistenciaHandler <|-- SesionActivaHandler : Extends
    AbstractAsistenciaHandler <|-- VigenciaQrHandler : Extends
    AbstractAsistenciaHandler <|-- DuplicidadAsistenciaHandler : Extends
    AbstractAsistenciaHandler <|-- InscripcionAcademicaHandler : Extends
    AbstractAsistenciaHandler <|-- CalculoPuntualidadHandler : Extends
    AbstractAsistenciaHandler ..> AsistenciaValidationContext : <<use>>
```

### 1.2. Diagrama de Diseno Procedimental (Interaccion Dinamica de Objetos en Tiempo de Ejecucion - `CU07`)

```mermaid
sequenceDiagram
    autonumber
    actor Estudiante as :Estudiante (Flutter/Web)
    participant Ctrl as :AsistenciaController
    participant Client as :AsistenciaServiceImpl (Client)
    participant Ctx as request:AsistenciaValidationContext
    participant H1 as h1:SesionActivaHandler
    participant H2 as h2:VigenciaQrHandler
    participant H3 as h3:DuplicidadAsistenciaHandler
    participant H4 as h4:InscripcionAcademicaHandler
    participant Feign as :AcademicoFeignClient
    participant H5 as h5:CalculoPuntualidadHandler
    participant Repo as :AsistenciaRepository

    Estudiante->>Ctrl: marcarAsistenciaQr(dto : MarcarAsistenciaQrDto)
    activate Ctrl
    Ctrl->>Client: registrarAsistenciaQr(dto)
    activate Client

    Client->>Ctx: <<create>> builder().registroEstudiante(...).codigoQr(...).build()
    Client->>H1: handleRequest(request)
    activate H1
    H1->>H1: findByCodigoQrGenerado(request.getCodigoQr())
    alt [sesion.estado == EstadoSesion.ACTIVA]
        H1->>Ctx: setSesionClase(sesion)
        H1->>H2: pasarAlSiguiente(request) -> handleRequest(request)
        activate H2
        alt [LocalDateTime.now() <= sesion.expiracionQr]
            H2->>H3: pasarAlSiguiente(request) -> handleRequest(request)
            activate H3
            H3->>H3: existsBySesionClaseIdAndRegistroEstudiante(id, registro)
            alt [!existeDuplicado]
                H3->>H4: pasarAlSiguiente(request) -> handleRequest(request)
                activate H4
                H4->>Feign: verificarInscripcion(registroEstudiante, idGrupoReferencia)
                activate Feign
                Feign-->>H4: verificacion : VerificacionInscripcionResponse
                deactivate Feign
                alt [verificacion.isInscrito() == true]
                    H4->>Ctx: setVerificacionInscripcion(verificacion)
                    H4->>H5: pasarAlSiguiente(request) -> handleRequest(request)
                    activate H5
                    H5->>H5: Duration.between(horaInicio, horaEvaluacion).toMinutes()
                    H5->>Ctx: setEstadoAsistenciaCalculado(PRESENTE / ATRASO)
                    H5-->>H4: return
                    deactivate H5
                else [no inscrito]
                    H4-->>Client: throw IllegalArgumentException
                end
                deactivate H4
            else [duplicado]
                H3-->>Client: throw IllegalStateException
            end
            deactivate H3
        else [QR expirado]
            H2-->>Client: throw IllegalStateException
        end
        deactivate H2
    else [sesion no activa o inexistente]
        H1-->>Client: throw IllegalArgumentException / IllegalStateException
    end
    deactivate H1

    Client->>Repo: save(asistencia : Asistencia)
    activate Repo
    Repo-->>Client: guardada : Asistencia
    deactivate Repo

    Client-->>Ctrl: response : AsistenciaResponseDto
    deactivate Client
    Ctrl-->>Estudiante: ResponseEntity<AsistenciaResponseDto> (201 CREATED)
    deactivate Ctrl
```

### 1.3. Especificacion PlantUML (`.puml`) — `CU07 Chain of Responsibility`
```plantuml
@startuml CU07_ChainOfResponsibility_UML25
skinparam classAttributeIconSize 0
skinparam linetype ortho

package "com.universidad.asistencia.service.impl" {
    class AsistenciaServiceImpl <<Client>> {
        - asistenciaRepository : AsistenciaRepository
        - sesionRepository : SesionClaseRepository
        - academicoClient : AcademicoFeignClient
        - abstractHandler : AbstractAsistenciaHandler
        + configurarCadenaResponsabilidad() : void
        + registrarAsistenciaQr(dto : MarcarAsistenciaQrDto) : AsistenciaResponseDto
    }
}

package "com.universidad.asistencia.chain" {
    class AsistenciaValidationContext <<Request>> {
        - registroEstudiante : String
        - codigoQr : String
        - observacion : String
        - fechaEvaluacion : LocalDate
        - horaEvaluacion : LocalTime
        - sesionClase : SesionClase
        - verificacionInscripcion : VerificacionInscripcionResponse
        - estadoAsistenciaCalculado : EstadoAsistencia
    }

    abstract class AbstractAsistenciaHandler {
        # siguiente : AbstractAsistenciaHandler
        + setSiguiente(siguiente : AbstractAsistenciaHandler) : AbstractAsistenciaHandler
        + getSiguiente() : AbstractAsistenciaHandler
        + {abstract} handleRequest(request : AsistenciaValidationContext) : void
        # pasarAlSiguiente(request : AsistenciaValidationContext) : void
    }
}

package "com.universidad.asistencia.chain.handler" {
    class SesionActivaHandler {
        - sesionRepository : SesionClaseRepository
        + handleRequest(request : AsistenciaValidationContext) : void
    }

    class VigenciaQrHandler {
        + handleRequest(request : AsistenciaValidationContext) : void
    }

    class DuplicidadAsistenciaHandler {
        - asistenciaRepository : AsistenciaRepository
        + handleRequest(request : AsistenciaValidationContext) : void
    }

    class InscripcionAcademicaHandler {
        - academicoClient : AcademicoFeignClient
        + handleRequest(request : AsistenciaValidationContext) : void
    }

    class CalculoPuntualidadHandler {
        - toleranciaMinutos : int
        + handleRequest(request : AsistenciaValidationContext) : void
    }
}

AsistenciaServiceImpl --> AbstractAsistenciaHandler : -abstractHandler
AbstractAsistenciaHandler o--> AbstractAsistenciaHandler : #siguiente
AbstractAsistenciaHandler <|-- SesionActivaHandler : Extends
AbstractAsistenciaHandler <|-- VigenciaQrHandler : Extends
AbstractAsistenciaHandler <|-- DuplicidadAsistenciaHandler : Extends
AbstractAsistenciaHandler <|-- InscripcionAcademicaHandler : Extends
AbstractAsistenciaHandler <|-- CalculoPuntualidadHandler : Extends
AbstractAsistenciaHandler ..> AsistenciaValidationContext : <<use>>
@enduml
```

---

## 2. CU03 - GESTIONAR ESTUDIANTE: Patron Strategy (UML 2.5+)

### 2.1. Diagrama de Clases Dinamico / Estructural de Diseno (UML 2.5+)

```mermaid
classDiagram
    direction TB

    class GrupoController {
        <<controller>>
        -importacionService : ImportacionEstudiantesService
        +importarEstudiantes(id : Long, archivo : MultipartFile, archivoPdf : MultipartFile) : ResponseEntity~CargaMasivaEstudiantesDto~
    }

    class ImportacionEstudiantesServiceImpl {
        <<service>>
        -grupoRepository : GrupoRepository
        -estudianteRepository : EstudianteRepository
        -boletaRepository : BoletaInscripcionRepository
        -authFeignClient : AuthFeignClient
        -contextoParser : ContextoParserArchivo
        +importarEstudiantesAGrupo(grupoId : Long, archivo : MultipartFile, archivoPdf : MultipartFile) : CargaMasivaEstudiantesDto
    }

    class ContextoParserArchivo {
        <<Contexto>>
        -estrategia : EstrategiaParserArchivo
        -estrategiasRegistradas : List~EstrategiaParserArchivo~
        +setEstrategia(estrategia : EstrategiaParserArchivo) : void
        +getEstrategia() : EstrategiaParserArchivo
        +ejecutarOperacion(inputStream : InputStream) : List~Map~String, String~~
        +seleccionarEstrategiaPorArchivo(nombreArchivo : String) : void
    }

    class EstrategiaParserArchivo {
        <<interface>>
        +operacion(inputStream : InputStream) : List~Map~String, String~~
        +soportaArchivo(nombreArchivo : String) : boolean
    }

    class ExcelParserConcreteStrategy {
        <<Concretec_A>>
        +soportaArchivo(nombreArchivo : String) : boolean
        +operacion(inputStream : InputStream) : List~Map~String, String~~
    }

    class PdfParserConcreteStrategy {
        <<Concretec_B>>
        +soportaArchivo(nombreArchivo : String) : boolean
        +operacion(inputStream : InputStream) : List~Map~String, String~~
    }

    class CsvParserConcreteStrategy {
        <<Concretec_N>>
        +soportaArchivo(nombreArchivo : String) : boolean
        +operacion(inputStream : InputStream) : List~Map~String, String~~
        -desescaparLineaCsvSiEsNecesario(linea : String) : String
        -dividirLineaCsv(linea : String, delimitador : String) : List~String~
    }

    GrupoController --> ImportacionEstudiantesServiceImpl : invoca
    ImportacionEstudiantesServiceImpl --> ContextoParserArchivo : -contextoParser (1)
    ContextoParserArchivo *--> EstrategiaParserArchivo : -estrategia (1)
    EstrategiaParserArchivo <|.. ExcelParserConcreteStrategy
    EstrategiaParserArchivo <|.. PdfParserConcreteStrategy
    EstrategiaParserArchivo <|.. CsvParserConcreteStrategy
```

### 2.2. Diagrama de Diseno Procedimental (Interaccion Dinamica de Objetos en Tiempo de Ejecucion - `CU03`)

```mermaid
sequenceDiagram
    autonumber
    actor Admin as :Administrador / Docente
    participant Ctrl as :GrupoController
    participant Svc as :ImportacionEstudiantesServiceImpl
    participant Ctx as contextoParser:ContextoParserArchivo
    participant Strat as estrategia:EstrategiaParserArchivo (Excel/PDF/CSV)
    participant Util as :NormalizadorCabecerasUtil
    participant EstRepo as :EstudianteRepository
    participant BolRepo as :BoletaInscripcionRepository
    participant Auth as :AuthFeignClient

    Admin->>Ctrl: importarEstudiantes(grupoId, archivo, archivoPdf)
    activate Ctrl
    Ctrl->>Svc: importarEstudiantesAGrupo(grupoId, archivo, archivoPdf)
    activate Svc

    Svc->>Ctx: seleccionarEstrategiaPorArchivo(nombreArchivo)
    activate Ctx
    loop Para cada item en estrategiasRegistradas
        Ctx->>Strat: soportaArchivo(nombreArchivo)
        Strat-->>Ctx: boolean
    end
    Ctx->>Ctx: setEstrategia(seleccionada)
    deactivate Ctx

    Svc->>Ctx: ejecutarOperacion(archivo.getInputStream())
    activate Ctx
    Ctx->>Strat: operacion(inputStream)
    activate Strat
    Strat->>Util: normalizarCabeceras(cabecerasRaw)
    Util-->>Strat: cabecerasNormalizadas : String[]
    Strat-->>Ctx: filas : List<Map<String, String>>
    deactivate Strat
    Ctx-->>Svc: filas : List<Map<String, String>>
    deactivate Ctx

    loop Para cada fila en filas
        Svc->>EstRepo: save(estudiante : Estudiante)
        EstRepo-->>Svc: guardado : Estudiante
        Svc->>BolRepo: save(boleta : BoletaInscripcion)
        Svc->>Auth: provisionarUsuario(request : ProvisionarUsuarioRequest)
    end

    Svc->>BolRepo: findBoletasByGrupoId(grupoId)
    BolRepo-->>Svc: boletasDelGrupo : List<BoletaInscripcion>
    Svc-->>Ctrl: CargaMasivaEstudiantesDto
    deactivate Svc
    Ctrl-->>Admin: ResponseEntity<CargaMasivaEstudiantesDto> (200 OK)
    deactivate Ctrl
```

### 2.3. Especificacion PlantUML (`.puml`) — `CU03 Strategy`
```plantuml
@startuml CU03_Strategy_UML25
skinparam classAttributeIconSize 0
skinparam linetype ortho

package "com.universidad.academico.service.impl" {
    class ImportacionEstudiantesServiceImpl {
        - grupoRepository : GrupoRepository
        - estudianteRepository : EstudianteRepository
        - boletaRepository : BoletaInscripcionRepository
        - authFeignClient : AuthFeignClient
        - contextoParser : ContextoParserArchivo
        + importarEstudiantesAGrupo(grupoId : Long, archivo : MultipartFile, archivoPdf : MultipartFile) : CargaMasivaEstudiantesDto
    }
}

package "com.universidad.academico.strategy" {
    class ContextoParserArchivo <<Contexto>> {
        - estrategia : EstrategiaParserArchivo
        - estrategiasRegistradas : List<EstrategiaParserArchivo>
        + setEstrategia(estrategia : EstrategiaParserArchivo) : void
        + getEstrategia() : EstrategiaParserArchivo
        + ejecutarOperacion(inputStream : InputStream) : List<Map<String, String>>
        + seleccionarEstrategiaPorArchivo(nombreArchivo : String) : void
    }

    interface EstrategiaParserArchivo <<interface>> {
        + operacion(inputStream : InputStream) : List<Map<String, String>>
        + soportaArchivo(nombreArchivo : String) : boolean
    }
}

package "com.universidad.academico.strategy.impl" {
    class ExcelParserConcreteStrategy <<Concretec_A>> {
        + operacion(inputStream : InputStream) : List<Map<String, String>>
        + soportaArchivo(nombreArchivo : String) : boolean
    }

    class PdfParserConcreteStrategy <<Concretec_B>> {
        + operacion(inputStream : InputStream) : List<Map<String, String>>
        + soportaArchivo(nombreArchivo : String) : boolean
    }

    class CsvParserConcreteStrategy <<Concretec_N>> {
        + operacion(inputStream : InputStream) : List<Map<String, String>>
        + soportaArchivo(nombreArchivo : String) : boolean
    }
}

ImportacionEstudiantesServiceImpl --> ContextoParserArchivo : -contextoParser
ContextoParserArchivo *--> EstrategiaParserArchivo : -estrategia
EstrategiaParserArchivo <|.. ExcelParserConcreteStrategy
EstrategiaParserArchivo <|.. PdfParserConcreteStrategy
EstrategiaParserArchivo <|.. CsvParserConcreteStrategy
@enduml
```

