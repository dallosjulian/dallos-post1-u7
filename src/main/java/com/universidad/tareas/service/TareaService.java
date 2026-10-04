package com.universidad.tareas.service;

import com.universidad.tareas.model.Prioridad;
import com.universidad.tareas.model.Tarea;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Capa de servicio en memoria para la gestión de tareas.
 * Componente singleton gestionado por el contenedor de Spring (@Service),
 * compartido concurrentemente por la capa web Thymeleaf y la API REST.
 *
 * @author Dallos
 */
@Service
public class TareaService {

    private final Map<Long, Tarea> tareas = new LinkedHashMap<>();
    private Long contadorId = 1L;

    /**
     * Constructor que inicializa el servicio precargando 3 tareas de ejemplo.
     */
    public TareaService() {
        inicializarDatosEjemplo();
    }

    private void inicializarDatosEjemplo() {
        guardar(new Tarea(
                "Diseñar arquitectura Spring Boot MVC y REST",
                "Modelar entidades, capas de servicio y controladores para la Unidad 7",
                Prioridad.ALTA,
                LocalDate.now().plusDays(2),
                false
        ));
        guardar(new Tarea(
                "Implementar plantillas Thymeleaf responsivas",
                "Construir lista.html y formulario.html con feedback de Bean Validation",
                Prioridad.MEDIA,
                LocalDate.now().plusDays(5),
                false
        ));
        guardar(new Tarea(
                "Revisar rúbrica de evaluación R2-Lab",
                "Verificar endpoints semánticos, decisiones de diseño y suite de pruebas",
                Prioridad.BAJA,
                LocalDate.now().plusDays(10),
                true
        ));
    }

    /**
     * Obtiene la lista completa de tareas registradas.
     *
     * @return Lista de tareas
     */
    public synchronized List<Tarea> obtenerTodas() {
        return new ArrayList<>(tareas.values());
    }

    /**
     * Filtra tareas combinando criterios opcionales de prioridad y estado de completitud.
     * Si un criterio es null, se ignora dicho filtro.
     *
     * @param prioridad   Criterio de prioridad (opcional)
     * @param completada  Criterio de estado completada/pendiente (opcional)
     * @return Lista de tareas que cumplen ambos criterios
     */
    public synchronized List<Tarea> filtrar(Prioridad prioridad, Boolean completada) {
        return tareas.values().stream()
                .filter(t -> prioridad == null || t.getPrioridad() == prioridad)
                .filter(t -> completada == null || t.isCompletada() == completada)
                .collect(Collectors.toList());
    }

    /**
     * Busca una tarea por su identificador único.
     *
     * @param id Identificador de la tarea
     * @return Optional con la tarea si existe, o Optional.empty()
     */
    public synchronized Optional<Tarea> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(tareas.get(id));
    }

    /**
     * Guarda o actualiza una tarea.
     * Si el ID es null, se genera un nuevo identificador secuencial.
     *
     * @param tarea Tarea a persistir
     * @return Tarea persistida con su ID asignado
     */
    public synchronized Tarea guardar(Tarea tarea) {
        if (tarea == null) {
            throw new IllegalArgumentException("La tarea no puede ser nula");
        }
        if (tarea.getId() == null) {
            tarea.setId(contadorId++);
        }
        tareas.put(tarea.getId(), tarea);
        return tarea;
    }

    /**
     * Marca una tarea existente como completada (completada = true).
     *
     * @param id Identificador de la tarea
     * @return Optional con la tarea modificada, o Optional.empty() si no existe
     */
    public synchronized Optional<Tarea> marcarCompletada(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        Tarea tarea = tareas.get(id);
        if (tarea != null) {
            tarea.setCompletada(true);
            return Optional.of(tarea);
        }
        return Optional.empty();
    }

    /**
     * Elimina una tarea por su identificador.
     *
     * @param id Identificador de la tarea a eliminar
     * @return true si la tarea existía y fue removida, false en caso contrario
     */
    public synchronized boolean eliminar(Long id) {
        if (id == null) {
            return false;
        }
        return tareas.remove(id) != null;
    }
}
