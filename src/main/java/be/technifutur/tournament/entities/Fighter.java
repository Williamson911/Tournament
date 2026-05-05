package be.technifutur.tournament.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "fighter")
public class Fighter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "style")
    private String fightingStyle;

    @Column(name = "origine")
    private String originCountry;


    @Column(name = "image")
    private String imgUrl;

    @OneToMany(mappedBy = "fighter", fetch = FetchType.LAZY)
    private List<Registration> registrations;
}
