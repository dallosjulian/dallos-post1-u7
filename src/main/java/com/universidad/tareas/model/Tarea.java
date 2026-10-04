package com.universidad.tareas.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad de dominio que representa una tarea en el sistema.
 * Contiene anotaciones de Bean Validation (Jakarta Validation)
 * y configuraciones de formato para Thymeleaf y Jackson.
 *
 * @author Dallos
 */
public class Tarea {

    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 3, max = 100, message = "El título debe tener entre 3 y 100 caracteres")
    private String titulo;

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String descripcion;

    @NotNull(message = "La prioridad es obligatoria")
    private Prioridad prioridad;

    @NotNull(message = "La fecha límite es obligatoria")
    @FutureOrPresent(message = "La fecha límite no puede ser anterior a hoy")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaLimite;

    private boolean completada;

    /**
     * Constructor por defecto sin argumentos requerido por frameworks
     * como Spring MVC, Thymeleaf y Jackson.
     */
    public Tarea() {
    }

    /**
     * Constructor con todos los argumentos.
     */
    public Tarea(Long id, String titulo, String descripcion, Prioridad prioridad, LocalDate fechaLimite, boolean completada) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.fechaLimite = fechaLimite;
        this.completada = completada;
    }

    /**
     * Constructor de conveniencia sin ID (para nuevas tareas antes de persistencia).
     */
    public Tarea(String titulo, String descripcion, Prioridad prioridad, LocalDate fechaLimite, boolean completada) {
        this(null, titulo, descripcion, prioridad, fechaLimite, completada);
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Prioridad prioridad) {
        this.prioridad = prioridad;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tarea tarea = (Tarea) o;
        return Objects.equals(id, tarea.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Tarea{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", prioridad=" + prioridad +
                ", fechaLimite=" + fechaLimite +
                ", completada=" + completada +
                '}';
    }
}
