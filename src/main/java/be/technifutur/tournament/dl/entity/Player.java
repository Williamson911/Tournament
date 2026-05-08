package be.technifutur.tournament.dl.entity;

import jakarta.persistence.*;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Player {
    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    @Column(nullable = false, unique = true)
    private String username;

    @Getter @Setter
    @Column(nullable = false, unique = true)
    private String email;

    @Getter @Setter
    private int age;

    @Getter @Setter
    private int elo;

    @Getter @Setter
    private String image;

    @Getter @Setter
    @JoinColumn(name = "id_fighter", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    private Fighter fighterMain;

//    @Column(name = "status", nullable = false)
//    @Enumerated(EnumType.STRING)
//    private TournamentStatus tournamentStatus;

}