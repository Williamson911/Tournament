package be.technifutur.tournament.entities;

import com.fasterxml.jackson.databind.deser.DataFormatReaders;
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
@Table(name = "player")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "player", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Registration> registrations;

    @OneToMany(mappedBy = "player1", fetch = FetchType.LAZY)
    private List<Match> matchesAsPlayer1;

    @OneToMany(mappedBy = "player2", fetch = FetchType.LAZY)
    private List<Match> matchesAsPlayer2;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}