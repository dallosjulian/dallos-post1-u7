package com.universidad.tareas.controller;

import com.universidad.tareas.model.Prioridad;
import com.universidad.tareas.model.Tarea;
import com.universidad.tareas.service.TareaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controlador RESTful para la gestión de tareas.
 * Expone endpoints HTTP semánticos (GET, POST, PUT, PATCH, DELETE) sobre el mismo
 * servicio de dominio compartido (singleton de Spring TareaService).
 *
 * @author Dallos
 */
@RestController
@RequestMapping("/api/tareas")
public class TareaApiController {

    private final TareaService tareaService;

    /**
     * Inyección explícita por constructor del bean singleton TareaService.
     *
     * @param tareaService Capa de servicio compartida
     */
    public TareaApiController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    /**
     * GET /api/tareas
     * Retorna la lista de tareas con soporte para filtrado opcional por prioridad y estado.
     * Código HTTP: 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<Tarea>> obtenerTareas(
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) Boolean completada) {
        List<Tarea> tareas = tareaService.filtrar(prioridad, completada);
        return ResponseEntity.ok(tareas);
    }

    /**
     * GET /api/tareas/{id}
     * Retorna una tarea específica por su ID.
     * Código HTTP: 200 OK si existe, 404 Not Found si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tarea> obtenerTareaPorId(@PathVariable Long id) {
        return tareaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/tareas
     * Crea una nueva tarea validando el cuerpo de la petición con Jakarta Bean Validation.
     * Código HTTP: 201 Created con cabecera Location (/api/tareas/{id}) y cuerpo con la tarea creada.
     */
    @PostMapping
    public ResponseEntity<Tarea> crearTarea(@Valid @RequestBody Tarea tarea) {
        // Aseguramos que la creación no sobrescriba un ID preexistente
        tarea.setId(null);
        Tarea tareaCreada = tareaService.guardar(tarea);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tareaCreada.getId())
                .toUri();

        return ResponseEntity.created(location).body(tareaCreada);
    }

    /**
     * PUT /api/tareas/{id}
     * Realiza un reemplazo total del recurso Tarea especificado por ID.
     * Requiere que todos los campos requeridos estén presentes en el payload.
     * Código HTTP: 200 OK con la tarea actualizada, o 404 Not Found si el ID no existe.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Tarea> actualizarTarea(
            @PathVariable Long id,
            @Valid @RequestBody Tarea tarea) {

        if (tareaService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        tarea.setId(id);
        Tarea tareaActualizada = tareaService.guardar(tarea);
        return ResponseEntity.ok(tareaActualizada);
    }

    /**
     * PATCH /api/tareas/{id}/completar
     * Modificación parcial: muta exclusivamente el atributo booleano 'completada' a true.
     * Código HTTP: 200 OK con la tarea completada, o 404 Not Found si no existe.
     */
    @PatchMapping("/{id}/completar")
    public ResponseEntity<Tarea> completarTarea(@PathVariable Long id) {
        return tareaService.marcarCompletada(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/tareas/{id}
     * Elimina el recurso identificado por su ID.
     * Código HTTP: 204 No Content si se eliminó con éxito, 404 Not Found si el recurso no existía.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarea(@PathVariable Long id) {
        boolean eliminada = tareaService.eliminar(id);
        if (eliminada) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
