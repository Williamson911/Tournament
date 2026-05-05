package be.technifutur.tournament.entities;

import be.technifutur.tournament.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "id_tournament", nullable = false)
    private int tournament;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus;

//    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "id_player1", nullable = false)
    private int idPlayer1;

//    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "id_player2", nullable = false)
    private int idPlayer2;

    @Column(name = "nb_rounds", nullable = false)
    private int roundNumber;

    @Column(name = "bracket_position")
    private String bracketPosition;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

//    @OneToOne(mappedBy = "match", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private MatchResult result;
}
