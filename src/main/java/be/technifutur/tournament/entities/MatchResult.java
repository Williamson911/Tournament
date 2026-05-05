package be.technifutur.tournament.entities;

import be.technifutur.tournament.enums.FinishType;
import be.technifutur.tournament.enums.RegistrationStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "match_result")
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_match", nullable = false, unique = true)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_winner", nullable = false)
    private Player winner;

    @Column(name = "player1_score", nullable = false)
    private Integer player1Score;

    @Column(name = "player2_score", nullable = false)
    private Integer player2Score;

    @Column(name = "nb_rounds", nullable = false)
    private Integer roundsPlayed;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FinishType finishType;
}
