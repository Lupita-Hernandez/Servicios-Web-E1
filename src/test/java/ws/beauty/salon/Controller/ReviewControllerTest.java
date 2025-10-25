package ws.beauty.salon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import ws.beauty.salon.dto.ReviewRequest;
import ws.beauty.salon.dto.ReviewResponse;
import ws.beauty.salon.service.ReviewService;
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

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    MockMvc mvc;
    
    @Autowired
    ObjectMapper mapper;

    @Autowired
    ReviewService service;

    private static final String BASE = "/api/v1/reviews";

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
        ReviewService reviewService() {
            return mock(ReviewService.class);
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
    private ReviewResponse resp(int idReview, Integer idClient, String firstName, Integer idService, 
                                String serviceName, String comment, Integer rating, String sentiment) {
        return ReviewResponse.builder()
                .idReview(idReview)
                .idClient(idClient)
                .firstName(firstName)
                .idService(idService)
                .serviceName(serviceName)
                .comment(comment)
                .rating(rating)
                .sentiment(sentiment)
                .build();
    }

    private ReviewRequest req(Integer idClient, String firstName, Integer idService, 
                             String serviceName, String comment, Integer rating, String sentiment) {
        ReviewRequest r = new ReviewRequest();
        r.setIdClient(idClient);
        r.setFirstName(firstName);
        r.setIdService(idService);
        r.setServiceName(serviceName);
        r.setComment(comment);
        r.setRating(rating);
        r.setSentiment(sentiment);
        return r;
    }

    /*
     * ==========================================
     * GET /api/v1/reviews/pagination?page=&pageSize=
     * ==========================================
     */

    @ParameterizedTest(name = "GET /pagination?page={0}&pageSize={1} → 200")
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
                List.of(resp(1, 100, "Juan", 10, "Corte de pelo", "Excelente servicio", 5, "POSITIVE"))
        );

        mvc.perform(get(BASE + "/pagination")
                .queryParam("page", String.valueOf(page))
                .queryParam("pageSize", String.valueOf(size))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].['id review']").value(1))
                .andExpect(jsonPath("$[0].comment").value("Excelente servicio"));
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
        when(service.findAllPaginated(page, size)).thenThrow(new IllegalArgumentException("Invalid paging params"));

        mvc.perform(get(BASE + "/pagination")
                .queryParam("page", String.valueOf(page))
                .queryParam("pageSize", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error", containsStringIgnoringCase("invalid")));
    }

    /*
     * ======================================
     * GET /api/v1/reviews/{id}
     * ======================================
     */

    @Test
    @DisplayName("GET /{id} existente → 200")
    void findById_ok() throws Exception {
        // Doc: Caso de éxito por id
        when(service.findById(5)).thenReturn(
                resp(5, 200, "María", 15, "Manicure", "Muy buen trabajo", 4, "POSITIVE")
        );

        mvc.perform(get(BASE + "/{id}", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['id review']").value(5))
                .andExpect(jsonPath("$.['id client']").value(200))
                .andExpect(jsonPath("$.['first name']").value("María"))
                .andExpect(jsonPath("$.['id service']").value(15))
                .andExpect(jsonPath("$.['service name']").value("Manicure"))
                .andExpect(jsonPath("$.comment").value("Muy buen trabajo"))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.sentiment").value("POSITIVE"));
    }

    @Test
    @DisplayName("GET /{id} no existente → 404")
    void findById_notFound() throws Exception {
        // Doc: No encontrado → 404 (Advice)
        when(service.findById(999)).thenThrow(new EntityNotFoundException("Review not found"));

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
     * POST /api/v1/reviews
     * ==============================
     */

    @Test
    @DisplayName("POST create válido → 201 + body")
    void create_ok() throws Exception {
        // Doc: Crea y devuelve 201
        ReviewRequest rq = req(100, "Carlos", 20, "Peinado", "Servicio impecable", 5, "POSITIVE");
        ReviewResponse created = resp(10, 100, "Carlos", 20, "Peinado", "Servicio impecable", 5, "POSITIVE");
        when(service.create(any(ReviewRequest.class))).thenReturn(created);

        mvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.['id review']").value(10))
                .andExpect(jsonPath("$.['id client']").value(100))
                .andExpect(jsonPath("$.['first name']").value("Carlos"))
                .andExpect(jsonPath("$.comment").value("Servicio impecable"))
                .andExpect(jsonPath("$.rating").value(5));
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
    @DisplayName("POST create con comment vacío → 400")
    void create_commentBlank() throws Exception {
        // Doc: Validación de @NotBlank en comment
        ReviewRequest rq = req(100, "Ana", 10, "Corte", "", 5, "POSITIVE");
        
        mvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST create con sentiment vacío → 400")
    void create_sentimentBlank() throws Exception {
        // Doc: Validación de @NotBlank en sentiment
        ReviewRequest rq = req(100, "Pedro", 10, "Corte", "Buen servicio", 4, "");
        
        mvc.perform(post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    /*
     * ==================================
     * PUT /api/v1/reviews/{id}
     * ==================================
     */

    @Test
    @DisplayName("PUT update válido → 200 con body actualizado")
    void update_ok() throws Exception {
        // Doc: Actualización exitosa
        ReviewRequest rq = req(200, "Laura", 25, "Tinte", "Actualizado: Excelente atención", 5, "POSITIVE");
        ReviewResponse updated = resp(3, 200, "Laura", 25, "Tinte", "Actualizado: Excelente atención", 5, "POSITIVE");
        when(service.update(eq(3), any(ReviewRequest.class))).thenReturn(updated);

        mvc.perform(put(BASE + "/{id}", 3)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['id review']").value(3))
                .andExpect(jsonPath("$.comment").value("Actualizado: Excelente atención"))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    @DisplayName("PUT update en no existente → 404")
    void update_notFound() throws Exception {
        // Doc: Servicio indica que no existe → 404
        when(service.update(eq(9999), any(ReviewRequest.class)))
                .thenThrow(new EntityNotFoundException("Review not found"));

        mvc.perform(put(BASE + "/{id}", 9999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req(1, "Test", 1, "Test", "Test comment", 3, "NEUTRAL"))))
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
     * GET /api/v1/reviews/client/{clientId}
     * =============================================
     */

    @Test
    @DisplayName("GET /client/{clientId} con resultados → 200 y lista")
    void getByClientId_ok() throws Exception {
        // Doc: Búsqueda por cliente
        when(service.findByClientId(100))
                .thenReturn(List.of(
                        resp(1, 100, "Juan", 10, "Corte", "Buen servicio", 4, "POSITIVE"),
                        resp(2, 100, "Juan", 15, "Barba", "Excelente", 5, "POSITIVE")
                ));

        mvc.perform(get(BASE + "/client/{clientId}", 100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].['id client']").value(100))
                .andExpect(jsonPath("$[1].['id client']").value(100));
    }

    @Test
    @DisplayName("GET /client/{clientId} sin resultados → 200 y []")
    void getByClientId_empty() throws Exception {
        // Doc: Sin coincidencias → 200 y arreglo vacío
        when(service.findByClientId(999)).thenReturn(Collections.emptyList());

        mvc.perform(get(BASE + "/client/{clientId}", 999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /*
     * =============================================
     * GET /api/v1/reviews/service/{serviceId}
     * =============================================
     */

    @Test
    @DisplayName("GET /service/{serviceId} con resultados → 200 y lista")
    void getByServiceId_ok() throws Exception {
        // Doc: Búsqueda por servicio
        when(service.findByServiceId(10))
                .thenReturn(List.of(
                        resp(1, 100, "Juan", 10, "Corte", "Buen corte", 4, "POSITIVE"),
                        resp(3, 200, "Ana", 10, "Corte", "Muy satisfecha", 5, "POSITIVE")
                ));

        mvc.perform(get(BASE + "/service/{serviceId}", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].['id service']").value(10))
                .andExpect(jsonPath("$[1].['id service']").value(10));
    }

    @Test
    @DisplayName("GET /service/{serviceId} sin resultados → 200 y []")
    void getByServiceId_empty() throws Exception {
        // Doc: Sin coincidencias → 200 y arreglo vacío
        when(service.findByServiceId(999)).thenReturn(Collections.emptyList());

        mvc.perform(get(BASE + "/service/{serviceId}", 999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /*
     * =============================================
     * GET /api/v1/reviews/sentiment/{sentiment}
     * =============================================
     */

    @Test
    @DisplayName("GET /sentiment/{sentiment} con resultados → 200 y lista")
    void getBySentiment_ok() throws Exception {
        // Doc: Búsqueda por sentimiento
        when(service.findBySentiment("POSITIVE"))
                .thenReturn(List.of(
                        resp(1, 100, "Carlos", 10, "Corte", "Excelente", 5, "POSITIVE"),
                        resp(2, 150, "María", 15, "Tinte", "Muy bueno", 5, "POSITIVE")
                ));

        mvc.perform(get(BASE + "/sentiment/{sentiment}", "POSITIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].sentiment").value("POSITIVE"))
                .andExpect(jsonPath("$[1].sentiment").value("POSITIVE"));
    }

    @Test
    @DisplayName("GET /sentiment/{sentiment} sin resultados → 200 y []")
    void getBySentiment_empty() throws Exception {
        // Doc: Sin coincidencias → 200 y arreglo vacío
        when(service.findBySentiment("INVALID_SENTIMENT")).thenReturn(Collections.emptyList());

        mvc.perform(get(BASE + "/sentiment/{sentiment}", "INVALID_SENTIMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /*
     * =============================================
     * GET /api/v1/reviews/rating/{rating}
     * =============================================
     */

    @Test
    @DisplayName("GET /rating/{rating} con resultados → 200 y lista")
    void getByRating_ok() throws Exception {
        // Doc: Búsqueda por rating mayor o igual
        when(service.findByRatingGreaterOrEqual(4))
                .thenReturn(List.of(
                        resp(1, 100, "Pedro", 10, "Corte", "Muy bueno", 4, "POSITIVE"),
                        resp(2, 150, "Sofía", 15, "Manicure", "Excelente", 5, "POSITIVE")
                ));

        mvc.perform(get(BASE + "/rating/{rating}", 4))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].rating").value(4))
                .andExpect(jsonPath("$[1].rating").value(5));
    }

    @Test
    @DisplayName("GET /rating/{rating} sin resultados → 200 y []")
    void getByRating_empty() throws Exception {
        // Doc: Sin coincidencias → 200 y arreglo vacío
        when(service.findByRatingGreaterOrEqual(5)).thenReturn(Collections.emptyList());

        mvc.perform(get(BASE + "/rating/{rating}", 5))
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
        when(service.findAllPaginated(0, 10)).thenReturn(Collections.emptyList());

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
        when(service.findAllPaginated(0, 10)).thenReturn(
                List.of(resp(1, 100, "Test", 10, "Test Service", "Test comment", 5, "POSITIVE"))
        );

        mvc.perform(get(BASE + "/pagination")
                .queryParam("page", "0")
                .queryParam("pageSize", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}