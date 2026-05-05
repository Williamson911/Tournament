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
@Table(name = "match")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_player1", nullable = false)
    private Player player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_player2", nullable = false)
    private Player player2;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "bracket_position")
    private String bracketPosition;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "played_at")
    private LocalDateTime playedAt;

    @OneToOne(mappedBy = "match", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private MatchResult result;
}
