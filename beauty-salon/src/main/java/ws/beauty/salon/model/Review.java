package ws.beauty.salon.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idReview")
    @JsonProperty("idReview")
    private Integer id;

    // Relación con Client
    /*@ManyToOne
    @JoinColumn(name = "client", referencedColumnName = "idClient", nullable = false)
    @JsonProperty("client")
    private Client client;

    // Relación con Service
    @ManyToOne
    @JoinColumn(name = "service", referencedColumnName = "idService", nullable = false)
    @JsonProperty("service")
    private Service service;*/

    @Column(name = "comment")
    @JsonProperty("comment")
    private String comment;

    @Column(name = "rating")
    @JsonProperty("rating")
    private Integer rating;

    @Column(name = "sentiment", length = 20)
    @JsonProperty("sentiment")
    private String sentiment;
}
