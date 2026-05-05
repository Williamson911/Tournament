package be.technifutur.tournament.entities;

import be.technifutur.tournament.enums.StatusTournament;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Tournament {

    @Id
    private UUID id;

    @Column(length = 50, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTournament status;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "tournament", fetch = FetchType.LAZY)
    private List<Registration> registrations;

    @OneToMany(mappedBy = "tournament", fetch = FetchType.LAZY)
    private List<Match> matches;

}


