package be.technifutur.tournament.entities;

import be.technifutur.tournament.enums.FinishType;
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
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus;

    @Getter
    @Column(name = "nb_rounds", nullable = false)
    private int numberRounds;

    @Getter
    @Column(name = "bracket_position")
    private String bracketPosition;

    @Getter
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Getter
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Getter
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Getter
    @Column(name = "player1_score", nullable = false)
    private Integer player1Score;

    @Getter
    @Column(name = "player2_score", nullable = false)
    private Integer player2Score;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tournament", nullable = false)
    private Tournament tournament;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_player1", nullable = false)
    private Player player1;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_player2", nullable = false)
    private Player player2;

    @Getter
    @Column(name = "finish_type")
    @Enumerated(EnumType.STRING)
    private FinishType finishType;

}
