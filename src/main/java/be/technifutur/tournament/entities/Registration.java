package be.technifutur.tournament.entities;

import be.technifutur.tournament.enums.StatusRegistration;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Registration {

    @EmbeddedId
    private RegistrationId id = new RegistrationId();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRegistration status;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    @MapsId("playerId")
    private Player player;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    @MapsId("tournamentId")
    private Tournament tournament;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    @MapsId("characterId")
    private CharacterSF character;



    @Embeddable
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    private static class RegistrationId implements Serializable {
        private UUID tournamentId;
        private UUID playerId;
        private UUID characterId;
    }
}

