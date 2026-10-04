package com.universidad.tareas.controller;

import com.universidad.tareas.model.Prioridad;
import com.universidad.tareas.model.Tarea;
import com.universidad.tareas.service.TareaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controlador MVC para la gestión de tareas mediante interfaz web Thymeleaf.
 * Aplica inyección de dependencias por constructor, validación con Bean Validation
 * y el patrón Post-Redirect-Get (PRG) para evitar envíos duplicados de formularios.
 *
 * @author Dallos
 */
@Controller
@RequestMapping("/tareas")
public class TareaController {

    private final TareaService tareaService;

    /**
     * Inyección explícita por constructor para garantizar inmutabilidad
     * y alta testabilidad (sin @Autowired en atributos).
     *
     * @param tareaService Servicio de tareas
     */
    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    /**
     * Muestra la lista de tareas con opciones de filtrado por prioridad y estado.
     */
    @GetMapping
    public String listarTareas(
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) Boolean completada,
            Model model) {

        List<Tarea> tareasFiltradas = tareaService.filtrar(prioridad, completada);
        model.addAttribute("tareas", tareasFiltradas);
        model.addAttribute("prioridades", Prioridad.values());
        model.addAttribute("prioridadSeleccionada", prioridad);
        model.addAttribute("completadaSeleccionada", completada);

        return "tareas/lista";
    }

    /**
     * Muestra el formulario para crear una nueva tarea.
     */
    @GetMapping("/nueva")
    public String mostrarFormularioNuevaTarea(Model model) {
        model.addAttribute("tarea", new Tarea());
        model.addAttribute("prioridades", Prioridad.values());
        model.addAttribute("accion", "Crear");
        return "tareas/formulario";
    }

    /**
     * Muestra el formulario para editar una tarea existente.
     */
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditarTarea(@PathVariable Long id, Model model) {
        Tarea tarea = tareaService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + id));

        model.addAttribute("tarea", tarea);
        model.addAttribute("prioridades", Prioridad.values());
        model.addAttribute("accion", "Editar");
        return "tareas/formulario";
    }

    /**
     * Procesa el guardado o actualización de una tarea.
     * Si hay errores de validación, recarga la vista del formulario con los mensajes de error.
     * Si los datos son válidos, guarda la tarea y redirige a la lista (Patrón PRG).
     */
    @PostMapping("/guardar")
    public String guardarTarea(
            @Valid @ModelAttribute("tarea") Tarea tarea,
            BindingResult resultado,
            Model model) {

        if (resultado.hasErrors()) {
            model.addAttribute("prioridades", Prioridad.values());
            model.addAttribute("accion", tarea.getId() != null ? "Editar" : "Crear");
            return "tareas/formulario";
        }

        tareaService.guardar(tarea);
        return "redirect:/tareas";
    }

    /**
     * Marca una tarea como completada utilizando método POST para preservar
     * la seguridad e idempotencia de las peticiones HTTP.
     */
    @PostMapping("/{id}/completar")
    public String completarTarea(@PathVariable Long id) {
        tareaService.marcarCompletada(id);
        return "redirect:/tareas";
    }

    /**
     * Elimina una tarea utilizando método POST con confirmación previa.
     */
    @PostMapping("/{id}/eliminar")
    public String eliminarTarea(@PathVariable Long id) {
        tareaService.eliminar(id);
        return "redirect:/tareas";
    }
}
