package ws.beauty.salon.model;


import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.JoinColumn;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Integer id;

    @Column(name = "userName", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @OneToOne
    @JoinColumn(name = "id_client", referencedColumnName = "id_client")
    private Client client;

    @OneToOne
    @JoinColumn(name = "id_stylist", referencedColumnName = "id_stylist")
    private Stylist stylist;

    @PrePersist
    @PreUpdate
    private void validateExclusiveRelations() {
        if (client != null && stylist != null) {
            throw new IllegalStateException("A user cannot be both a client and a stylist at the same time.");
        }
        if (client == null && stylist == null) {
            throw new IllegalStateException("A stylist must be assigned to the stylist role.");
        }
    }
}
