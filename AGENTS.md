# PRINCIPIOS DE ARQUITECTURA Y DISEÑO
*   **Clean Architecture y Clean Code:** El código debe ser limpio, legible y estructurado en capas: Controller (Presentación), Service (Casos de Uso / Lógica de Negocio), Repository (Infraestructura / Persistencia) y Entity/Domain (Dominio). Uso de DTOs para transferencia de datos.
*   **Alta Cohesión y Bajo Acoplamiento:** Es OBLIGATORIO respetar este principio. Los módulos deben tener responsabilidades únicas y bien definidas. Un microservicio no debe hacer JOINs directos a las tablas de otro microservicio; deben comunicarse mediante peticiones HTTP/REST a través de OpenFeign o eventos asíncronos.
*   **Principios SOLID:** Aplicar invariablemente en la construcción de clases e interfaces. Utilizar inyección de dependencias a través de constructores.

# ESTÁNDARES DE CODIFICACIÓN
Se deben respetar estrictamente las siguientes convenciones de nomenclatura:

## Backend (Java/Spring Boot)
*   Clases / Interfaces / Records: PascalCase
*   Métodos / Funciones: camelCase
*   Variables / Propiedades / Instancias: camelCase
*   Paquetes (Packages): minúsculas (lowercase) sin guiones
*   Constantes: UPPER_SNAKE_CASE

## Frontend (JS/React/Flutter)
*   Clases / Interfaces: PascalCase
*   Métodos / Funciones: camelCase
*   Variables / Propiedades: camelCase
*   Archivos / Módulos: PascalCase (para Componentes) / camelCase (para Hooks o Servicios)
*   Constantes: UPPER_SNAKE_CASE

# REGLAS DE INTERFAZ Y FORMATO (STRICT)
*   **PROHIBICIÓN DE EMOJIS:** Queda TOTALMENTE PROHIBIDO el uso de emojis en las interfaces gráficas de usuario (GUI), en los comentarios del código, en los logs y en las respuestas generadas por la IA.
*   Todo el código generado debe estar comentado detalladamente para facilitar el aprendizaje, modularizado y listo para pruebas.
*   Proporciona documentación exhaustiva y explica cómo implementar el código paso a paso.
