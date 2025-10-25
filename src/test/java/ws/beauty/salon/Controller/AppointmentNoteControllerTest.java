package ws.beauty.salon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import ws.beauty.salon.dto.AppointmentNoteRequest;
import ws.beauty.salon.dto.AppointmentNoteResponse;
import ws.beauty.salon.service.AppointmentNoteService;
import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentNoteController.class)
class AppointmentNoteControllerTest {

    @Autowired
    MockMvc mvc;
    
    @Autowired
    ObjectMapper mapper;

    @Autowired
    AppointmentNoteService service;

    private static final String BASE = "/api/v1/appointment-notes";

    @BeforeEach
    void beforeEach() {
        reset(service);
    }

    /*
     * ===========================
     * Config de test: mock beans
     * ===========================
     */
    @TestConfiguration
    static class TestConfig {
        @Bean
        AppointmentNoteService appointmentNoteService() {
            return mock(AppointmentNoteService.class);
        }
        
        @Bean
        ModelMapper modelMapper() {
            return new ModelMapper();
        }
    }

    /*
     * ===========================
     * Helpers DTO
     * ===========================
     */
    private AppointmentNoteResponse resp(int idNote, Integer idAppointment, String noteText) {
        return AppointmentNoteResponse.builder()
                .idNote(idNote)
                .idAppointment(idAppointment)
                .noteText(noteText)
                .build();
    }

    private AppointmentNoteRequest req(Integer idAppointment, String noteText) {
        AppointmentNoteRequest r = new AppointmentNoteRequest();
        r.setIdAppointment(idAppointment);
        r.setNoteText(noteText);
        return r;
    }

    /*
     * ==========================================
     * GET /api/v1/appointment-notes/paginated?page=&size=
     * ==========================================
     */

    @ParameterizedTest(name = "GET /paginated?page={0}&size={1} → 200")
    @CsvSource({
            "0,10",
            "1,5",
            "2,20",
            "3,3"
    })
    @DisplayName("GET paginado: parámetros válidos")
    void pagination_ok(int page, int size) throws Exception {
        // Doc: Parámetros correctos devuelven 200
        when(service.findAllPaginated(page, size)).thenReturn(
                List.of(resp(1, 100, "Cliente llegó 10 minutos antes"))
        );

        mvc.perform(get(BASE + "/paginated")
                .queryParam("page", String.valueOf(page))
                .queryParam("size", String.valueOf(size))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].['id note']").value(1))
                .andExpect(jsonPath("$[0].['id appointment']").value(100))
                .andExpect(jsonPath("$[0].['note text']").value("Cliente llegó 10 minutos antes"));
    }

    @ParameterizedTest(name = "GET /paginated?page={0}&size={1} inválidos → 400")
    @CsvSource({
            "-1,10",
            "0,0",
            "0,-5",
            "-3,-3"
    })
    @DisplayName("GET paginado: parámetros inválidos → 400")
    void pagination_badRequest(int page, int size) throws Exception {
        // Doc: El servicio valida y lanza IllegalArgumentException → 400 (Advice)
        when(service.findAllPaginated(page, size)).thenThrow(new IllegalArgumentException("Invalid paging params"));

        mvc.perform(get(BASE + "/paginated")
                .queryParam("page", String.valueOf(page))
                .queryParam("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error", containsStringIgnoringCase("invalid")));
    }

    @Test
    @DisplayName("GET /paginated sin resultados → 200 y []")
    void pagination_empty() throws Exception {
        // Doc: Sin notas → 200 y arreglo vacío
        when(service.findAllPaginated(0, 10)).thenReturn(Collections.emptyList());

        mvc.perform(get(BASE + "/paginated")
                .queryParam("page", "0")
                .queryParam("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /*
     * ======================================
     * GET /api/v1/appointment-notes/{id}
     * ======================================
     */

    @Test
    @DisplayName("GET /{id} existente → 200")
    void findById_ok() throws Exception {
        // Doc: Caso de éxito por id
        when(service.findById(5)).thenReturn(
                resp(5, 200, "El cliente solicitó un cambio de peinado al final del servicio")
        );

        mvc.perform(get(BASE + "/{id}", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['id note']").value(5))
                .andExpect(jsonPath("$.['id appointment']").value(200))
                .andExpect(jsonPath("$.['note text']").value("El cliente solicitó un cambio de peinado al final del servicio"));
    }

    @Test
    @DisplayName("GET /{id} no existente → 404")
    void findById_notFound() throws Exception {
        // Doc: No encontrado → 404 (Advice)
        when(service.findById(999)).thenThrow(new EntityNotFoundException("Note not found"));

        mvc.perform(get(BASE + "/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /{id} no numérico → 400 (binding)")
    void findById_badPath() throws Exception {
        // Doc: Conversión fallida de path variable → 400
        mvc.perform(get(BASE + "/xyz"))
                .andExpect(status().isBadRequest());
    }

    /*
     * ==============================
     * POST /api/v1/appointment-notes
     * ==============================
     */

    @Test
    @DisplayName("POST create válido → 200 + body")
    void create_ok() throws Exception {
        // Doc: Crea y devuelve 200
        AppointmentNoteRequest rq = req(100, "El cliente llegó temprano y solicitó corte clásico");
        AppointmentNoteResponse created = resp(10, 100, "El cliente llegó temprano y solicitó corte clásico");
        when(service.create(any(AppointmentNoteRequest.class))).thenReturn(created);

        mvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['id note']").value(10))
                .andExpect(jsonPath("$.['id appointment']").value(100))
                .andExpect(jsonPath("$.['note text']").value("El cliente llegó temprano y solicitó corte clásico"));
    }

    @Test
    @DisplayName("POST create inválido → 400 por @Valid")
    void create_invalidBody() throws Exception {
        // Doc: Body sin campos requeridos → 400 (MethodArgumentNotValidException)
        mvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST create con noteText vacío → 400")
    void create_noteTextBlank() throws Exception {
        // Doc: Validación de @NotBlank en noteText
        AppointmentNoteRequest rq = req(100, "");
        
        mvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST create con noteText muy largo → 400")
    void create_noteTextTooLong() throws Exception {
        // Doc: Validación de @Size(max=500)
        AppointmentNoteRequest rq = req(100, "a".repeat(501));
        
        mvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST create con appointment inexistente → 404")
    void create_appointmentNotFound() throws Exception {
        // Doc: Appointment no existe → 404
        when(service.create(any(AppointmentNoteRequest.class)))
                .thenThrow(new EntityNotFoundException("Appointment not found"));

        mvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req(9999, "Nota de prueba"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    /*
     * ==================================
     * PUT /api/v1/appointment-notes/{id}
     * ==================================
     */

    @Test
    @DisplayName("PUT update válido → 200 con body actualizado")
    void update_ok() throws Exception {
        // Doc: Actualización exitosa
        AppointmentNoteRequest rq = req(200, "Nota actualizada: Cliente muy satisfecho con el servicio");
        AppointmentNoteResponse updated = resp(3, 200, "Nota actualizada: Cliente muy satisfecho con el servicio");
        when(service.update(eq(3), any(AppointmentNoteRequest.class))).thenReturn(updated);

        mvc.perform(put(BASE + "/{id}", 3)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['id note']").value(3))
                .andExpect(jsonPath("$.['id appointment']").value(200))
                .andExpect(jsonPath("$.['note text']").value("Nota actualizada: Cliente muy satisfecho con el servicio"));
    }

    @Test
    @DisplayName("PUT update en no existente → 404")
    void update_notFound() throws Exception {
        // Doc: Servicio indica que no existe → 404
        when(service.update(eq(9999), any(AppointmentNoteRequest.class)))
                .thenThrow(new EntityNotFoundException("Note not found"));

        mvc.perform(put(BASE + "/{id}", 9999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req(1, "Test note"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT update con body inválido → 400 por @Valid")
    void update_invalidBody() throws Exception {
        // Doc: Falla de validación → 400
        mvc.perform(put(BASE + "/{id}", 10)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT update con noteText vacío → 400")
    void update_noteTextBlank() throws Exception {
        // Doc: Validación de @NotBlank
        AppointmentNoteRequest rq = req(100, "");
        
        mvc.perform(put(BASE + "/{id}", 5)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT update con noteText muy largo → 400")
    void update_noteTextTooLong() throws Exception {
        // Doc: Validación de @Size(max=500)
        AppointmentNoteRequest rq = req(100, "a".repeat(501));
        
        mvc.perform(put(BASE + "/{id}", 5)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT update con appointment inexistente → 404")
    void update_appointmentNotFound() throws Exception {
        // Doc: Appointment no existe → 404
        when(service.update(eq(5), any(AppointmentNoteRequest.class)))
                .thenThrow(new EntityNotFoundException("Appointment not found"));

        mvc.perform(put(BASE + "/{id}", 5)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req(9999, "Nota válida"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    /*
     * =====================================
     * Headers: CORS / Content Negotiation
     * =====================================
     */

    @Test
    @DisplayName("CORS: Access-Control-Allow-Origin")
    void cors_header_present() throws Exception {
        when(service.findAllPaginated(0, 10)).thenReturn(Collections.emptyList());

        mvc.perform(get(BASE + "/paginated")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .header("Origin", "https://beauty-salon.com")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://beauty-salon.com"));
    }

    @Test
    @DisplayName("Content negotiation: Accept JSON → application/json")
    void contentNegotiation_json() throws Exception {
        when(service.findAllPaginated(0, 10)).thenReturn(
                List.of(resp(1, 100, "Nota de prueba"))
        );

        mvc.perform(get(BASE + "/paginated")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}