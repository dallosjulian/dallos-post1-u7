package com.universidad.tareas.model;

/**
 * Enum que representa los niveles de prioridad de una tarea.
 *
 * @author Dallos
 */
public enum Prioridad {
    ALTA("Alta"),
    MEDIA("Media"),
    BAJA("Baja");

    private final String etiqueta;

    Prioridad(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
