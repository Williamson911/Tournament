package be.technifutur.tournament.entities;

import be.technifutur.tournament.enums.RegistrationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

//    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "id_player", nullable = false)
    private int idPlayer;

    //    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "id_fighter", nullable = false)
    private int idFighter;

//    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "id_tournament", nullable = false)
    private int idTournament;

    @Column(name = "registered_date", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Column(name="status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RegistrationStatus registrationStatus;

    @PrePersist
    protected void onCreate() {
        this.registeredAt = LocalDateTime.now();
    }
}
