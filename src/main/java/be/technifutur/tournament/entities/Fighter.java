package be.technifutur.tournament.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String style;

    @Column(name = "origine")
    private String originCountry;

    @Column(name = "image")
    private String image;

    @JsonIgnore
    @OneToMany(mappedBy = "fighter", fetch = FetchType.LAZY)
    private List<Registration> registrations;
}