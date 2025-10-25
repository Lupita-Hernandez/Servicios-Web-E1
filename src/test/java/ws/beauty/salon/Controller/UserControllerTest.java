package ws.beauty.salon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import ws.beauty.salon.dto.UserRequest;
import ws.beauty.salon.dto.UserResponse;
import ws.beauty.salon.service.UserService;
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

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mvc;
    
    @Autowired
    ObjectMapper mapper;

    @Autowired
    UserService service;

    private static final String BASE = "/api/v1/users";

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
        UserService userService() {
            return mock(UserService.class);
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
    private UserResponse resp(int id, String username, String role) {
        return UserResponse.builder()
                .id(id)
                .username(username)
                .role(role)
                .build();
    }

    private UserRequest req(String username, String password, String role) {
        UserRequest r = new UserRequest();
        r.setUsername(username);
        r.setPassword(password);
        r.setRole(role);
        return r;
    }

    /*
     * ==========================================
     * GET /api/v1/users/pagination?page=&pageSize=
     * ==========================================
     */

    @ParameterizedTest(name = "GET /pagination?page={0}&pageSize={1} → 200")
    @CsvSource({
            "0,10",
            "1,1",
            "2,50",
            "5,5"
    })
    @DisplayName("GET paginado: parámetros válidos")
    void pagination_ok(int page, int size) throws Exception {
        // Doc: Parámetros correctos devuelven 200
        when(service.getAll(page, size)).thenReturn(List.of(resp(100, "john_doe", "ADMIN")));

        mvc.perform(get(BASE + "/pagination")
                .queryParam("page", String.valueOf(page))
                .queryParam("pageSize", String.valueOf(size))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].['id user']").value(100))
                .andExpect(jsonPath("$[0].['user name']").value("john_doe"));
    }

    @ParameterizedTest(name = "GET /pagination?page={0}&pageSize={1} inválidos → 400")
    @CsvSource({
            "-1,10",
            "0,0",
            "0,-5",
            "-3,-3"
    })
    @DisplayName("GET paginado: parámetros inválidos → 400")
    void pagination_badRequest(int page, int size) throws Exception {
        // Doc: El servicio valida y lanza IllegalArgumentException → 400 (Advice)
        when(service.getAll(page, size)).thenThrow(new IllegalArgumentException("Invalid paging params"));

        mvc.perform(get(BASE + "/pagination")
                .queryParam("page", String.valueOf(page))
                .queryParam("pageSize", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error", containsStringIgnoringCase("invalid")));
    }

    /*
     * ======================================
     * GET /api/v1/users/{id}
     * ======================================
     */

    @Test
    @DisplayName("GET /{id} existente → 200")
    void findById_ok() throws Exception {
        // Doc: Caso de éxito por id
        when(service.findById(7)).thenReturn(resp(7, "maria_lopez", "CLIENT"));

        mvc.perform(get(BASE + "/{id}", 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['id user']").value(7))
                .andExpect(jsonPath("$.['user name']").value("maria_lopez"))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test
    @DisplayName("GET /{id} no existente → 404")
    void findById_notFound() throws Exception {
        // Doc: No encontrado → 404 (Advice)
        when(service.findById(999)).thenThrow(new EntityNotFoundException("User not found"));

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
     * POST /api/v1/users
     * ==============================
     */

    @Test
    @DisplayName("POST create válido → 201 + body")
    void create_ok() throws Exception {
        // Doc: Crea y devuelve 201
        UserRequest rq = req("ana_garcia", "password123", "STYLIST");
        UserResponse created = resp(1234, "ana_garcia", "STYLIST");
        when(service.create(any(UserRequest.class))).thenReturn(created);

        mvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.['id user']").value(1234))
                .andExpect(jsonPath("$.['user name']").value("ana_garcia"))
                .andExpect(jsonPath("$.role").value("STYLIST"));
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
    @DisplayName("POST create con username muy largo → 400")
    void create_usernameTooLong() throws Exception {
        // Doc: Validación de @Size
        UserRequest rq = req("a".repeat(51), "password123", "ADMIN");
        
        mvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    /*
     * ==================================
     * PUT /api/v1/users/{id}
     * ==================================
     */

    @Test
    @DisplayName("PUT update válido → 200 con body actualizado")
    void update_ok() throws Exception {
        // Doc: Actualización exitosa
        UserRequest rq = req("usuario_editado", "newpass456", "ADMIN");
        UserResponse updated = resp(55, "usuario_editado", "ADMIN");
        when(service.update(eq(55), any(UserRequest.class))).thenReturn(updated);

        mvc.perform(put(BASE + "/{id}", 55)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['id user']").value(55))
                .andExpect(jsonPath("$.['user name']").value("usuario_editado"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("PUT update en no existente → 404")
    void update_notFound() throws Exception {
        // Doc: Servicio indica que no existe → 404
        when(service.update(eq(9999), any(UserRequest.class)))
                .thenThrow(new EntityNotFoundException("User not found"));

        mvc.perform(put(BASE + "/{id}", 9999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req("test", "pass", "CLIENT"))))
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
     * GET /api/v1/users/role/{role}
     * =============================================
     */

    @Test
    @DisplayName("GET /role/{role} con resultados → 200 y lista")
    void getByRole_ok() throws Exception {
        // Doc: Búsqueda por role
        when(service.findByRole("ADMIN"))
                .thenReturn(List.of(resp(1, "admin1", "ADMIN"), resp(2, "admin2", "ADMIN")));

        mvc.perform(get(BASE + "/role/{role}", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].role").value("ADMIN"))
                .andExpect(jsonPath("$[1].role").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /role/{role} sin resultados → 200 y []")
    void getByRole_empty() throws Exception {
        // Doc: Sin coincidencias → 200 y arreglo vacío
        when(service.findByRole("INVALID_ROLE")).thenReturn(Collections.emptyList());

        mvc.perform(get(BASE + "/role/{role}", "INVALID_ROLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /*
     * =============================================
     * GET /api/v1/users/search/{username}
     * =============================================
     */

    @Test
    @DisplayName("GET search con resultados → 200 y lista")
    void searchByUsername_ok() throws Exception {
        // Doc: Búsqueda por username (case-insensitive parcial)
        when(service.findByUsername("john"))
                .thenReturn(List.of(resp(1, "john_doe", "CLIENT"), resp(3, "johnny", "STYLIST")));

        mvc.perform(get(BASE + "/search/{username}", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].['user name']", containsStringIgnoringCase("john")));
    }

    @Test
    @DisplayName("GET search sin resultados → 200 y []")
    void searchByUsername_empty() throws Exception {
        // Doc: Sin coincidencias → 200 y arreglo vacío
        when(service.findByUsername("zzz")).thenReturn(Collections.emptyList());

        mvc.perform(get(BASE + "/search/{username}", "zzz"))
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
        when(service.getAll(0, 10)).thenReturn(Collections.emptyList());

        mvc.perform(get(BASE + "/pagination")
                .queryParam("page", "0")
                .queryParam("pageSize", "10")
                .header("Origin", "https://beauty-salon.com")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://beauty-salon.com"));
    }

    @Test
    @DisplayName("Content negotiation: Accept JSON → application/json")
    void contentNegotiation_json() throws Exception {
        when(service.getAll(0, 10)).thenReturn(List.of(resp(1, "test", "CLIENT")));

        mvc.perform(get(BASE + "/pagination")
                .queryParam("page", "0")
                .queryParam("pageSize", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}