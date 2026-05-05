package be.technifutur.tournament.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "character ")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CharacterSF {

    @Id
    private UUID id;

    @Column(length = 100, nullable = false, unique = true)
    private String name;

    @Column(length = 100)
    private String stule;

    @Column(length = 100)
    private String origin;

    @Column(length = 255)
    private String image;

    @OneToMany(mappedBy = "character", fetch = FetchType.LAZY)
    private List<Registration> registrations;

}
