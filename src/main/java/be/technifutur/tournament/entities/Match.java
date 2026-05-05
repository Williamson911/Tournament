package be.technifutur.tournament.entities;

import be.technifutur.tournament.enums.StatusMatch;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Match {

    @EmbeddedId
    private MatchId id = new MatchId();

    private int nbRounds;

    @Column(length = 50)
    private String bracketPos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMatch status;

    @Column(nullable = false)
    LocalDateTime playedAt;

    @Column(nullable = false)
    LocalDateTime scheduledAt;

    @OneToOne (mappedBy = "match",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY)
    private MatchResult match;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    @MapsId("tournamentId")
    private Tournament tournament;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    @MapsId("playerId1")
    private Player player1;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    @MapsId("playerId2")
    private Player player2;

    @OneToOne (optional = false,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY)
    private MatchResult matchResult;



    @Embeddable
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    private static class MatchId implements Serializable {
        private UUID tournamentID;
        private UUID playerId1;
        private UUID playerId2;

    }
}
