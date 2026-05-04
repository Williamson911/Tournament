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
@Table(name = "character_sf")
public class Fighter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "fighting_style")
    private String fightingStyle;

    @Column(name = "origin_country")
    private String originCountry;

    @Column
    private String tier;

    @Column(name = "img_url")
    private String imgUrl;

    @OneToMany(mappedBy = "character", fetch = FetchType.LAZY)
    private List<Registration> registrations;
}