 ┌──────────────────────┐       ┌────────────────────────┐
 │ App escritorio       │       │ App móvil Android      │
 │ Administrativo       │       │ Estudiante             │
 │ Docente              │       │ - Escanea QR           │
 │ JavaFX               │       │ - Se identifica        │
 │                      │       │ - Registra offline     │
 └──────────┬───────────┘       │ - Sincroniza después   │
            │                   └──────────┬─────────────┘
            │ HTTPS / REST                 │ HTTPS / REST
            └──────────────┬───────────────┘
                           │
                   ┌───────▼─────────┐
                   │ API Gateway     │
                   │ Spring Cloud    │
                   └───────┬─────────┘
                           │
      ┌────────────────────┼──────────────────────┐
      │                    │                      │
┌─────▼──────┐      ┌──────▼───────┐       ┌──────▼─────────┐
│auth-service│      │ academico-   │       │ asistencia-    │
│            │      │ service      │       │ service        │
│ Usuarios   │      │ Docentes     │       │ Sesiones       │
│ Roles/JWT  │      │ Materias     │       │ QR             │
└─────┬──────┘      │ Grupos       │       │ Asistencias    │
      │             │ Horarios     │       │ Sincronización │
      │             └──────┬───────┘       └──────┬─────────┘
      │                    │                      │
      └────────────────────┴───────────┬──────────┘
                                       │
                               ┌───────▼─────────┐
                               │ PostgreSQL      │
                               │ Datos académicos│
                               │ y asistencia    │
                               └─────────────────┘