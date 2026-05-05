package be.technifutur.tournament.entities;

import be.technifutur.tournament.enums.FinishType;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "match_result")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MatchResult {

    @EmbeddedId
    private MatchResultId id = new MatchResultId();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinishType finishType;

    private int player1Score;

    private int player2Score;


    @Embeddable
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    private static class MatchResultId implements Serializable {
        private UUID matchId;
        private UUID winnerId;

    }
}
