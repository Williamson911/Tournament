package be.technifutur.tournament.entities;

import be.technifutur.tournament.enums.RegistrationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Registration {
    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    @Column(name="status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RegistrationStatus registrationStatus;

    @Getter @Setter
    @Column(name = "registered_date", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Getter @Setter
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "id_player", nullable = false)
    private Player player;

    @Getter @Setter
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "id_tournament", nullable = false)
    private Tournament tournament;

    @PrePersist
    protected void onCreate() {
        this.registeredAt = LocalDateTime.now();
    }
}
