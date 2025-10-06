package ws.beauty.salon.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "stylists")
public class Stylist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idStylist")
    @JsonProperty("idStylist")
    private Integer id;

    @Column(name = "firstName", nullable = false, length = 50)
    @JsonProperty("firstNname")
    private String firstName;

    @Column(name = "lastName", nullable = false, length = 50)
    @JsonProperty("lastName")
    private String lastName;

    @Column(name = "specialty", length = 100)
    @JsonProperty("specialty")
    private String specialty;

    @Column(name = "workSchedule", columnDefinition = "TEXT")
    @JsonProperty("workSchedule")
    private String workSchedule;

    @Column(name = "available", nullable = false)
    @JsonProperty("available")
    private Boolean available = true;
}