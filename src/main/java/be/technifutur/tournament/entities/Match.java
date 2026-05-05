package be.technifutur.tournament.entities;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tournament", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_player1", nullable = false)
    private Player player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_player2", nullable = false)
    private Player player2;

    @Column(name = "nb_rounds", nullable = false)
    private Integer numberRounds;

    @Column(name = "bracket_position")
    private String bracketPosition;

    @Column(nullable = false)
    private String status;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime started_at;

    @Column(name = "finished_at")
    private LocalDateTime finished_at;

    @OneToOne(mappedBy = "match", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private MatchResult result;
}
