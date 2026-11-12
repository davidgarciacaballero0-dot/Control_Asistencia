package com.universidad.asistencia.chain;

/**
 * Clase abstracta AbstractHandler del patron de diseno Chain of Responsibility.
 * Sigue estrictamente el diagrama de clases de referencia:
 * - Posee una relacion de auto-agregacion (#siguiente : AbstractAsistenciaHandler).
 * - Define la operacion publica +handleRequest(request) que cada ConcreteHandler extiende e implementa.
 * - Provee el metodo protegido #pasarAlSiguiente(request) para delegar al sucesor en la cadena.
 */
public abstract class AbstractAsistenciaHandler {

    /**
     * Referencia de auto-agregacion al siguiente manejador en la cadena de responsabilidad.
     */
    protected AbstractAsistenciaHandler siguiente;

    /**
     * Establece el siguiente eslabon en la cadena y lo retorna para permitir encadenamiento fluido.
     *
     * @param siguiente Manejador concreto que procesara la solicitud a continuacion.
     * @return La instancia del siguiente manejador configurado.
     */
    public AbstractAsistenciaHandler setSiguiente(AbstractAsistenciaHandler siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    /**
     * Obtiene la referencia al siguiente manejador en la cadena.
     *
     * @return El siguiente manejador o null si este es el ultimo eslabon.
     */
    public AbstractAsistenciaHandler getSiguiente() {
        return this.siguiente;
    }

    /**
     * Procesa la solicitud de validacion de asistencia.
     * Cada ConcreteHandler extiende esta clase e implementa su regla de negocio especifica.
     *
     * @param request Contexto de validacion con los datos de la peticion y estado acumulado.
     */
    public abstract void handleRequest(AsistenciaValidationContext request);

    /**
     * Propaga la solicitud al siguiente manejador de la cadena si existe uno configurado.
     *
     * @param request Contexto de validacion actual.
     */
    protected void pasarAlSiguiente(AsistenciaValidationContext request) {
        if (this.siguiente != null) {
            this.siguiente.handleRequest(request);
        }
    }
}

