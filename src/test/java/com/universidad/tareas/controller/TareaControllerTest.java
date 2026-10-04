package com.universidad.tareas.controller;

import com.universidad.tareas.model.Prioridad;
import com.universidad.tareas.model.Tarea;
import com.universidad.tareas.service.TareaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración web para TareaController (Thymeleaf MVC).
 */
@WebMvcTest(TareaController.class)
class TareaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TareaService tareaService;

    @Test
    @DisplayName("GET /tareas debe mostrar lista.html con tareas y atributos de modelo")
    void debeListarTareas() throws Exception {
        Tarea tarea1 = new Tarea(1L, "Tarea 1", "Desc 1", Prioridad.ALTA, LocalDate.now().plusDays(1), false);
        when(tareaService.filtrar(null, null)).thenReturn(Arrays.asList(tarea1));

        mockMvc.perform(get("/tareas"))
                .andExpect(status().isOk())
                .andExpect(view().name("tareas/lista"))
                .andExpect(model().attributeExists("tareas", "prioridades"))
                .andExpect(model().attribute("tareas", Arrays.asList(tarea1)));
    }

    @Test
    @DisplayName("GET /tareas/nueva debe mostrar formulario.html con tarea vacía y accion=Crear")
    void debeMostrarFormularioNuevaTarea() throws Exception {
        mockMvc.perform(get("/tareas/nueva"))
                .andExpect(status().isOk())
                .andExpect(view().name("tareas/formulario"))
                .andExpect(model().attributeExists("tarea", "prioridades", "accion"))
                .andExpect(model().attribute("accion", "Crear"));
    }

    @Test
    @DisplayName("GET /tareas/{id}/editar debe cargar tarea existente y mostrar formulario.html con accion=Editar")
    void debeMostrarFormularioEditarTarea() throws Exception {
        Tarea tarea = new Tarea(1L, "Tarea 1", "Desc 1", Prioridad.ALTA, LocalDate.now().plusDays(1), false);
        when(tareaService.buscarPorId(1L)).thenReturn(Optional.of(tarea));

        mockMvc.perform(get("/tareas/1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("tareas/formulario"))
                .andExpect(model().attributeExists("tarea", "prioridades", "accion"))
                .andExpect(model().attribute("accion", "Editar"))
                .andExpect(model().attribute("tarea", tarea));
    }

    @Test
    @DisplayName("POST /tareas/guardar con datos válidos debe guardar y redirigir a /tareas (PRG)")
    void debeGuardarTareaValidaYRedirigir() throws Exception {
        Tarea tareaGuardada = new Tarea(1L, "Tarea Valida", "Desc", Prioridad.ALTA, LocalDate.now().plusDays(1), false);
        when(tareaService.guardar(any(Tarea.class))).thenReturn(tareaGuardada);

        mockMvc.perform(post("/tareas/guardar")
                        .param("titulo", "Tarea Válida")
                        .param("descripcion", "Descripción válida de la tarea")
                        .param("prioridad", "ALTA")
                        .param("fechaLimite", LocalDate.now().plusDays(2).toString())
                        .param("completada", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tareas"));

        verify(tareaService, times(1)).guardar(any(Tarea.class));
    }

    @Test
    @DisplayName("POST /tareas/guardar con datos inválidos debe recargar formulario.html con errores sin redirect")
    void debeRecargarFormularioCuandoHayErroresDeValidacion() throws Exception {
        mockMvc.perform(post("/tareas/guardar")
                        .param("titulo", "a") // Inválido: min 3
                        .param("descripcion", "Desc")
                        .param("prioridad", "ALTA")
                        .param("fechaLimite", LocalDate.now().minusDays(1).toString()) // Inválido: en el pasado
                        .param("completada", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("tareas/formulario"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("tarea", "titulo", "fechaLimite"));

        verify(tareaService, never()).guardar(any(Tarea.class));
    }

    @Test
    @DisplayName("POST /tareas/{id}/completar debe marcar como completada y redirigir")
    void debeCompletarTareaYRedirigir() throws Exception {
        mockMvc.perform(post("/tareas/1/completar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tareas"));

        verify(tareaService, times(1)).marcarCompletada(1L);
    }

    @Test
    @DisplayName("POST /tareas/{id}/eliminar debe eliminar tarea y redirigir")
    void debeEliminarTareaYRedirigir() throws Exception {
        mockMvc.perform(post("/tareas/1/eliminar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tareas"));

        verify(tareaService, times(1)).eliminar(1L);
    }
}
