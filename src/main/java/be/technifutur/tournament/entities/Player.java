package be.technifutur.tournament.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Player {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name="age")
    private int age;

    @Column(name = "elo")
    private String elo;

    @Column(name = "image")
    private String image;

//    @Column(name = "status", nullable = false)
//    @Enumerated(EnumType.STRING)
//    private TournamentStatus tournamentStatus;

}