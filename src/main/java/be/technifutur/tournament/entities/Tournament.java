package be.technifutur.tournament.entities;

import be.technifutur.tournament.enums.TournamentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

//    @OneToMany(mappedBy = "tournament", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private List<Registration> registrations;
//
//    @OneToMany(mappedBy = "tournament", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private List<Match> matches;
}
