package ws.beauty.salon.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUser")
    @JsonProperty("idUuser")
    private Integer id;

    @Column(name = "userName", unique = true, nullable = false, length = 50)
    @JsonProperty("userName")
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    @JsonProperty("password")
    private String passwordHash;

    @Column(name = "role", nullable = false, length = 20)
    @JsonProperty("role")
    private String role;

    // Relación con Client
    /*@OneToOne
    @JoinColumn(name = "client", referencedColumnName = "idClient")
    @JsonProperty("client")
    private Client client;

    // Relación con Stylist
    @OneToOne
    @JoinColumn(name = "stylist", referencedColumnName = "idStylist")
    @JsonProperty("stylist")
    private Stylist stylist;*/
}