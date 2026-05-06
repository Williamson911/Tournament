package be.technifutur.tournament.entities;

import be.technifutur.tournament.enums.FinishType;
import be.technifutur.tournament.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Match {
    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @Getter @Setter
    @Column(name = "nb_rounds", nullable = false)
    private int numberRounds;

    @Getter @Setter
    @Column(name = "bracket_position")
    private String bracketPosition;

    @Getter @Setter
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Getter @Setter
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Getter @Setter
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Getter @Setter
    @Column(name = "player1_score", nullable = false)
    private Integer player1Score;

    @Getter @Setter
    @Column(name = "player2_score", nullable = false)
    private Integer player2Score;

    @Getter @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "id_tournament", nullable = false)
    private Tournament tournament;

    @Getter @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "id_player1", nullable = false)
    private Player player1;

    @Getter @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "id_player2", nullable = false)
    private Player player2;

    @Getter @Setter
    @Column(name = "finish_type")
    @Enumerated(EnumType.STRING)
    private FinishType finishType;

}
