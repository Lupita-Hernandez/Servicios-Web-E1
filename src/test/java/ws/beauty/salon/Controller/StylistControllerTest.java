package ws.beauty.salon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import ws.beauty.salon.dto.StylistRequest;
import ws.beauty.salon.dto.StylistResponse;
import ws.beauty.salon.service.StylistService;
import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StylistController.class)
class StylistControllerTest {

    @Autowired
    MockMvc mvc;
    
    @Autowired
    ObjectMapper mapper;

    @Autowired
    StylistService service;

    private static final String BASE = "/api/v1/stylists";

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
        StylistService stylistService() {
            return mock(StylistService.class);
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
    private StylistResponse resp(int id, String firstName, String lastName, String specialty, String workSchedule, Boolean available) {
        return StylistResponse.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .specialty(specialty)
                .workSchedule(workSchedule)
                .available(available)
                .build();
    }

    private StylistRequest req(String firstName, String lastName, String specialty, String workSchedule, Boolean available) {
        return StylistRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .specialty(specialty)
                .workSchedule(workSchedule)
                .available(available)
                .build();
    }

    /*
     * ==========================================
     * GET /api/v1/stylists
     * ==========================================
     */

    @Test
    @DisplayName("GET /stylists → 200 con lista de estilistas")
    void getAll_ok() throws Exception {
        // Doc: Obtener todos los estilistas
        when(service.findAll()).thenReturn(List.of(
                resp(1, "Ana", "García", "Colorista", "Lun-Vie 9-17", true),
                resp(2, "Carlos", "López", "Barbería", "Mar-Sab 10-18", false)
        ));

        mvc.perform(get(BASE)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].['id stylist']").value(1))
                .andExpect(jsonPath("$[0].['first name']").value("Ana"))
                .andExpect(jsonPath("$[0].['last name']").value("García"))
                .andExpect(jsonPath("$[0].specialty").value("Colorista"))
                .andExpect(jsonPath("$[1].['id stylist']").value(2))
                .andExpect(jsonPath("$[1].available").value(false));
    }

    @Test
    @DisplayName("GET /stylists sin resultados → 200 y []")
    void getAll_empty() throws Exception {
        // Doc: Sin estilistas registrados
        when(service.findAll()).thenReturn(Collections.emptyList());

        mvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /*
     * ======================================
     * GET /api/v1/stylists/{id}
     * ======================================
     */

    @Test
    @DisplayName("GET /{id} existente → 200")
    void findById_ok() throws Exception {
        // Doc: Caso de éxito por id
        when(service.findById(5)).thenReturn(
                resp(5, "María", "Rodríguez", "Peinados", "Lun-Vie 8-16", true)
        );

        mvc.perform(get(BASE + "/{id}", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['id stylist']").value(5))
                .andExpect(jsonPath("$.['first name']").value("María"))
                .andExpect(jsonPath("$.['last name']").value("Rodríguez"))
                .andExpect(jsonPath("$.specialty").value("Peinados"))
                .andExpect(jsonPath("$.['work schedule']").value("Lun-Vie 8-16"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @DisplayName("GET /{id} no existente → 404")
    void findById_notFound() throws Exception {
        // Doc: No encontrado → 404 (Advice)
        when(service.findById(999)).thenThrow(new EntityNotFoundException("Stylist not found"));

        mvc.perform(get(BASE + "/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /{id} no numérico → 400 (binding)")
    void findById_badPath() throws Exception {
        // Doc: Conversión fallida de path variable → 400
        mvc.perform(get(BASE + "/abc"))
                .andExpect(status().isBadRequest());
    }

    /*
     * ==============================
     * POST /api/v1/stylists
     * ==============================
     */

    @Test
    @DisplayName("POST create válido → 201 + body")
    void create_ok() throws Exception {
        // Doc: Crea y devuelve 201
        StylistRequest rq = req("Pedro", "Martínez", "Corte y diseño", "Lun-Sab 9-18", true);
        StylistResponse created = resp(10, "Pedro", "Martínez", "Corte y diseño", "Lun-Sab 9-18", true);
        when(service.create(any(StylistRequest.class))).thenReturn(created);

        mvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.['id stylist']").value(10))
                .andExpect(jsonPath("$.['first name']").value("Pedro"))
                .andExpect(jsonPath("$.['last name']").value("Martínez"))
                .andExpect(jsonPath("$.specialty").value("Corte y diseño"))
                .andExpect(jsonPath("$.available").value(true));
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
    @DisplayName("POST create con firstName vacío → 400")
    void create_firstNameBlank() throws Exception {
        // Doc: Validación de @NotBlank
        StylistRequest rq = req("", "López", "Colorista", "Lun-Vie 9-17", true);
        
        mvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST create con lastName muy largo → 400")
    void create_lastNameTooLong() throws Exception {
        // Doc: Validación de @Size
        StylistRequest rq = req("Ana", "a".repeat(51), "Peinados", "Mar-Sab 10-18", true);
        
        mvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST create con specialty muy larga → 400")
    void create_specialtyTooLong() throws Exception {
        // Doc: Validación de @Size en specialty
        StylistRequest rq = req("Carlos", "Pérez", "a".repeat(101), "Lun-Vie 8-16", false);
        
        mvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    /*
     * ==================================
     * PUT /api/v1/stylists/{id}
     * ==================================
     */

    @Test
    @DisplayName("PUT update válido → 200 con body actualizado")
    void update_ok() throws Exception {
        // Doc: Actualización exitosa
        StylistRequest rq = req("Laura", "Fernández", "Manicure y pedicure", "Lun-Vie 10-19", false);
        StylistResponse updated = resp(3, "Laura", "Fernández", "Manicure y pedicure", "Lun-Vie 10-19", false);
        when(service.update(eq(3), any(StylistRequest.class))).thenReturn(updated);

        mvc.perform(put(BASE + "/{id}", 3)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['id stylist']").value(3))
                .andExpect(jsonPath("$.['first name']").value("Laura"))
                .andExpect(jsonPath("$.['last name']").value("Fernández"))
                .andExpect(jsonPath("$.specialty").value("Manicure y pedicure"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    @DisplayName("PUT update en no existente → 404")
    void update_notFound() throws Exception {
        // Doc: Servicio indica que no existe → 404
        when(service.update(eq(9999), any(StylistRequest.class)))
                .thenThrow(new EntityNotFoundException("Stylist not found"));

        mvc.perform(put(BASE + "/{id}", 9999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req("Test", "Test", "Test", "Test", true))))
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

    /*
     * =============================================
     * GET /api/v1/stylists/specialty/{specialty}
     * =============================================
     */

    @Test
    @DisplayName("GET /specialty/{specialty} con resultados → 200 y lista")
    void getBySpecialty_ok() throws Exception {
        // Doc: Búsqueda por especialidad
        when(service.getBySpecialty("Colorista"))
                .thenReturn(List.of(
                        resp(1, "Ana", "García", "Colorista", "Lun-Vie 9-17", true),
                        resp(4, "Sofia", "Ruiz", "Colorista", "Mar-Sab 10-18", true)
                ));

        mvc.perform(get(BASE + "/specialty/{specialty}", "Colorista"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].specialty").value("Colorista"))
                .andExpect(jsonPath("$[1].specialty").value("Colorista"));
    }

    @Test
    @DisplayName("GET /specialty/{specialty} sin resultados → 200 y []")
    void getBySpecialty_empty() throws Exception {
        // Doc: Sin coincidencias → 200 y arreglo vacío
        when(service.getBySpecialty("Especialidad inexistente")).thenReturn(Collections.emptyList());

        mvc.perform(get(BASE + "/specialty/{specialty}", "Especialidad inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /*
     * =============================================
     * GET /api/v1/stylists/available
     * =============================================
     */

    @Test
    @DisplayName("GET /available con resultados → 200 y lista")
    void getAvailable_ok() throws Exception {
        // Doc: Búsqueda de estilistas disponibles
        when(service.getAvailableStylists())
                .thenReturn(List.of(
                        resp(1, "Ana", "García", "Colorista", "Lun-Vie 9-17", true),
                        resp(5, "María", "Rodríguez", "Peinados", "Lun-Vie 8-16", true)
                ));

        mvc.perform(get(BASE + "/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].available").value(true));
    }

    @Test
    @DisplayName("GET /available sin resultados → 200 y []")
    void getAvailable_empty() throws Exception {
        // Doc: Sin estilistas disponibles → 200 y arreglo vacío
        when(service.getAvailableStylists()).thenReturn(Collections.emptyList());

        mvc.perform(get(BASE + "/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /*
     * =====================================
     * Headers: CORS / Content Negotiation
     * =====================================
     */

    @Test
    @DisplayName("CORS: Access-Control-Allow-Origin")
    void cors_header_present() throws Exception {
        when(service.findAll()).thenReturn(Collections.emptyList());

        mvc.perform(get(BASE)
                .header("Origin", "https://beauty-salon.com")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://beauty-salon.com"));
    }

    @Test
    @DisplayName("Content negotiation: Accept JSON → application/json")
    void contentNegotiation_json() throws Exception {
        when(service.findAll()).thenReturn(List.of(
                resp(1, "Test", "User", "Test", "Test", true)
        ));

        mvc.perform(get(BASE)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}