package be.technifutur.tournament.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Player {

    @Id
    private UUID id;

    @Column(length = 50, nullable = false, unique = true)
    private String username;

    @Column(length = 150, nullable = false, unique = true)
    private String email;

    @Column(length = 255)
    private String image;

    private int age;

    private int elo;

    @OneToMany(mappedBy = "player", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Registration> registrations;

    @OneToMany(mappedBy = "player1", fetch = FetchType.LAZY)
    private List<Match> matchesAsPlayer1;

    @OneToMany(mappedBy = "player2", fetch = FetchType.LAZY)
    private List<Match> matchesAsPlayer2;

}
