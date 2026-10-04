package com.universidad.tareas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.universidad.tareas.model.Prioridad;
import com.universidad.tareas.model.Tarea;
import com.universidad.tareas.service.TareaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para TareaApiController y ApiErrorHandler.
 */
@WebMvcTest(TareaApiController.class)
@Import(ApiErrorHandler.class)
class TareaApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TareaService tareaService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("GET /api/tareas debe retornar 200 OK con array JSON de tareas")
    void debeObtenerTodasLasTareas() throws Exception {
        Tarea tarea1 = new Tarea(1L, "Tarea 1", "Desc 1", Prioridad.ALTA, LocalDate.now().plusDays(1), false);
        Tarea tarea2 = new Tarea(2L, "Tarea 2", "Desc 2", Prioridad.MEDIA, LocalDate.now().plusDays(2), true);
        when(tareaService.filtrar(null, null)).thenReturn(Arrays.asList(tarea1, tarea2));

        mockMvc.perform(get("/api/tareas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].titulo", is("Tarea 1")))
                .andExpect(jsonPath("$[0].prioridad", is("ALTA")));
    }

    @Test
    @DisplayName("GET /api/tareas/{id} con ID existente debe retornar 200 OK con el objeto Tarea")
    void debeObtenerTareaPorIdExistente() throws Exception {
        Tarea tarea = new Tarea(1L, "Tarea Existente", "Desc", Prioridad.ALTA, LocalDate.now().plusDays(1), false);
        when(tareaService.buscarPorId(1L)).thenReturn(Optional.of(tarea));

        mockMvc.perform(get("/api/tareas/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.titulo", is("Tarea Existente")));
    }

    @Test
    @DisplayName("GET /api/tareas/{id} con ID inexistente debe retornar 404 Not Found")
    void debeRetornar404CuandoTareaNoExiste() throws Exception {
        when(tareaService.buscarPorId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tareas/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/tareas con payload válido debe retornar 201 Created con Location header")
    void debeCrearTareaValida() throws Exception {
        Tarea nueva = new Tarea("Nueva Tarea REST", "Descripción", Prioridad.ALTA, LocalDate.now().plusDays(3), false);
        Tarea creada = new Tarea(10L, "Nueva Tarea REST", "Descripción", Prioridad.ALTA, LocalDate.now().plusDays(3), false);

        when(tareaService.guardar(any(Tarea.class))).thenReturn(creada);

        mockMvc.perform(post("/api/tareas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/tareas/10")))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.titulo", is("Nueva Tarea REST")));
    }

    @Test
    @DisplayName("POST /api/tareas con datos inválidos debe retornar 400 Bad Request y JSON de errores")
    void debeRetornar400ConErroresDeValidacion() throws Exception {
        Tarea invalida = new Tarea("", "Desc", null, LocalDate.now().minusDays(1), false);

        mockMvc.perform(post("/api/tareas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.titulo", notNullValue()))
                .andExpect(jsonPath("$.prioridad", notNullValue()))
                .andExpect(jsonPath("$.fechaLimite", notNullValue()));

        verify(tareaService, never()).guardar(any(Tarea.class));
    }

    @Test
    @DisplayName("PUT /api/tareas/{id} con ID existente debe actualizar completamente y retornar 200 OK")
    void debeActualizarTareaExistente() throws Exception {
        Tarea existente = new Tarea(1L, "Tarea Previa", "Desc", Prioridad.BAJA, LocalDate.now().plusDays(1), false);
        Tarea reemplazo = new Tarea(1L, "Tarea Actualizada", "Nueva Desc", Prioridad.ALTA, LocalDate.now().plusDays(4), true);

        when(tareaService.buscarPorId(1L)).thenReturn(Optional.of(existente));
        when(tareaService.guardar(any(Tarea.class))).thenReturn(reemplazo);

        mockMvc.perform(put("/api/tareas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reemplazo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.titulo", is("Tarea Actualizada")))
                .andExpect(jsonPath("$.completada", is(true)));
    }

    @Test
    @DisplayName("PUT /api/tareas/{id} con ID inexistente debe retornar 404 Not Found")
    void debeRetornar404AlActualizarTareaInexistente() throws Exception {
        Tarea tarea = new Tarea(999L, "No Existe", "Desc", Prioridad.ALTA, LocalDate.now().plusDays(2), false);
        when(tareaService.buscarPorId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/tareas/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tarea)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/tareas/{id}/completar debe marcar completada=true y retornar 200 OK")
    void debeCompletarTareaParcialmente() throws Exception {
        Tarea completada = new Tarea(1L, "Tarea", "Desc", Prioridad.ALTA, LocalDate.now().plusDays(1), true);
        when(tareaService.marcarCompletada(1L)).thenReturn(Optional.of(completada));

        mockMvc.perform(patch("/api/tareas/1/completar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.completada", is(true)));
    }

    @Test
    @DisplayName("PATCH /api/tareas/{id}/completar con ID inexistente debe retornar 404 Not Found")
    void debeRetornar404AlCompletarTareaInexistente() throws Exception {
        when(tareaService.marcarCompletada(999L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/tareas/999/completar"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/tareas/{id} existente debe retornar 204 No Content")
    void debeEliminarTareaExistente() throws Exception {
        when(tareaService.eliminar(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/tareas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/tareas/{id} inexistente debe retornar 404 Not Found")
    void debeRetornar404AlEliminarTareaInexistente() throws Exception {
        when(tareaService.eliminar(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/tareas/999"))
                .andExpect(status().isNotFound());
    }
}
