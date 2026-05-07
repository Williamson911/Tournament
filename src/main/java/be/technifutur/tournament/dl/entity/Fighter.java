package be.technifutur.tournament.dl.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Fighter {
    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    @Column(name="name", nullable = false)
    private String name;

    @Getter @Setter
    @Column(name = "style")
    private String style;

    @Getter @Setter
    @Column(name = "origine")
    private String originCountry;

    @Getter @Setter
    @Column(name = "image")
    private String image;

}