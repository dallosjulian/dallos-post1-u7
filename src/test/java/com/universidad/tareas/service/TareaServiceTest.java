package com.universidad.tareas.service;

import com.universidad.tareas.model.Prioridad;
import com.universidad.tareas.model.Tarea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para TareaService.
 */
class TareaServiceTest {

    private TareaService tareaService;

    @BeforeEach
    void setUp() {
        tareaService = new TareaService();
    }

    @Test
    @DisplayName("Debe inicializar el servicio con 3 tareas precargadas")
    void debeInicializarConTareasPrecargadas() {
        List<Tarea> todas = tareaService.obtenerTodas();
        assertEquals(3, todas.size(), "Debería haber 3 tareas iniciales");
    }

    @Test
    @DisplayName("Debe filtrar correctamente por prioridad y estado")
    void debeFiltrarPorPrioridadYEstado() {
        List<Tarea> tareasAlta = tareaService.filtrar(Prioridad.ALTA, null);
        assertFalse(tareasAlta.isEmpty());
        assertTrue(tareasAlta.stream().allMatch(t -> t.getPrioridad() == Prioridad.ALTA));

        List<Tarea> completadas = tareaService.filtrar(null, true);
        assertFalse(completadas.isEmpty());
        assertTrue(completadas.stream().allMatch(Tarea::isCompletada));

        List<Tarea> pendientesMedia = tareaService.filtrar(Prioridad.MEDIA, false);
        assertTrue(pendientesMedia.stream().allMatch(t -> t.getPrioridad() == Prioridad.MEDIA && !t.isCompletada()));
    }

    @Test
    @DisplayName("Debe buscar una tarea por ID exitosamente")
    void debeBuscarPorId() {
        Optional<Tarea> tareaOpt = tareaService.buscarPorId(1L);
        assertTrue(tareaOpt.isPresent());
        assertEquals(1L, tareaOpt.get().getId());

        Optional<Tarea> noExiste = tareaService.buscarPorId(999L);
        assertTrue(noExiste.isEmpty());
    }

    @Test
    @DisplayName("Debe guardar y asignar ID a una nueva tarea")
    void debeGuardarNuevaTarea() {
        Tarea nueva = new Tarea("Nueva Tarea Test", "Descripción de prueba", Prioridad.ALTA, LocalDate.now().plusDays(3), false);
        Tarea guardada = tareaService.guardar(nueva);

        assertNotNull(guardada.getId());
        assertEquals("Nueva Tarea Test", guardada.getTitulo());

        Optional<Tarea> encontrada = tareaService.buscarPorId(guardada.getId());
        assertTrue(encontrada.isPresent());
    }

    @Test
    @DisplayName("Debe marcar una tarea como completada")
    void debeMarcarTareaComoCompletada() {
        Optional<Tarea> tareaOpt = tareaService.marcarCompletada(1L);
        assertTrue(tareaOpt.isPresent());
        assertTrue(tareaOpt.get().isCompletada());

        Optional<Tarea> noExiste = tareaService.marcarCompletada(999L);
        assertTrue(noExiste.isEmpty());
    }

    @Test
    @DisplayName("Debe eliminar una tarea existente")
    void debeEliminarTarea() {
        boolean eliminada = tareaService.eliminar(2L);
        assertTrue(eliminada);
        assertTrue(tareaService.buscarPorId(2L).isEmpty());

        boolean noEliminada = tareaService.eliminar(999L);
        assertFalse(noEliminada);
    }
}
