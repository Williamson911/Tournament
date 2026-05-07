package be.technifutur.tournament.entities;

import be.technifutur.tournament.enums.TournamentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Tournament {

    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TournamentStatus status;

    @Getter @Setter
    @Column(nullable = false)
    private String name;

    @Getter @Setter
    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Getter @Setter
    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Getter @Setter
    @Column(name = "has_group_stage")
    private boolean hasGroupStage;
}
