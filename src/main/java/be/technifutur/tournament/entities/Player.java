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

    @Column(name = "image")
    private String image;

    @Column(name = "elo")
    private String elo;

    @Column(name="age")
    private int age;


    @OneToMany(mappedBy = "player", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Registration> registrations;

    @OneToMany(mappedBy = "player1", fetch = FetchType.LAZY)
    private List<Match> matchesAsPlayer1;

    @OneToMany(mappedBy = "player2", fetch = FetchType.LAZY)
    private List<Match> matchesAsPlayer2;

}