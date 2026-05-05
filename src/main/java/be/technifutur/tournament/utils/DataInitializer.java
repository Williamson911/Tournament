package be.technifutur.tournament.utils;

import be.technifutur.tournament.entities.*;
import be.technifutur.tournament.enums.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDateTime;

@ApplicationScoped
public class DataInitializer {

    @Inject
    private EntityManagerFactory emf;

    public void init(@Observes Startup startup) {

        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // =====================
            // 🎮 FIGHTERS
            // =====================
            Fighter jin = Fighter.builder().name("Jin Kazama").style("Karate").originCountry("Japan").build();
            Fighter kazuya = Fighter.builder().name("Kazuya Mishima").style("Mishima Karate").originCountry("Japan").build();
            Fighter king = Fighter.builder().name("King").style("Wrestling").originCountry("Mexico").build();
            Fighter nina = Fighter.builder().name("Nina Williams").style("Assassination").originCountry("Ireland").build();

            em.persist(jin);
            em.persist(kazuya);
            em.persist(king);
            em.persist(nina);

            // =====================
            // 👤 PLAYERS
            // =====================
            Player p1 = Player.builder().username("kevin").email("kevin@test.be").elo("1200").age(25).build();
            Player p2 = Player.builder().username("laura").email("laura@test.be").elo("1250").age(23).build();
            Player p3 = Player.builder().username("yassine").email("yassine@test.be").elo("1300").age(27).build();
            Player p4 = Player.builder().username("sofia").email("sofia@test.be").elo("1100").age(22).build();

            em.persist(p1);
            em.persist(p2);
            em.persist(p3);
            em.persist(p4);

            // =====================
            // 🏆 TOURNAMENT
            // =====================
            Tournament t = Tournament.builder()
                    .name("Tekken 8 Championship")
                    .tournamentStatus(TournamentStatus.IN_PROGRESS)
                    .startDate(LocalDateTime.now())
                    .build();

            em.persist(t);

            // =====================
            // 📝 REGISTRATIONS (fighter obligatoire)
            // =====================
            em.persist(Registration.builder()
                    .player(p1)
                    .tournament(t)
                    .fighter(jin)
                    .registrationStatus(RegistrationStatus.CONFIRMED)
                    .build());

            em.persist(Registration.builder()
                    .player(p2)
                    .tournament(t)
                    .fighter(kazuya)
                    .registrationStatus(RegistrationStatus.CONFIRMED)
                    .build());

            em.persist(Registration.builder()
                    .player(p3)
                    .tournament(t)
                    .fighter(king)
                    .registrationStatus(RegistrationStatus.CONFIRMED)
                    .build());

            em.persist(Registration.builder()
                    .player(p4)
                    .tournament(t)
                    .fighter(nina)
                    .registrationStatus(RegistrationStatus.CONFIRMED)
                    .build());

            // =====================
            // ⚔️ MATCHES
            // =====================
            Match m1 = Match.builder()
                    .tournament(t)
                    .player1(p1)
                    .player2(p2)
                    .numberRounds(3)
                    .matchStatus(MatchStatus.FINISHED)
                    .scheduledAt(LocalDateTime.now().minusDays(1))
                    .playedAt(LocalDateTime.now().minusDays(1))
                    .build();

            Match m2 = Match.builder()
                    .tournament(t)
                    .player1(p3)
                    .player2(p4)
                    .numberRounds(3)
                    .matchStatus(MatchStatus.FINISHED)
                    .scheduledAt(LocalDateTime.now().minusDays(1))
                    .playedAt(LocalDateTime.now().minusDays(1))
                    .build();

            Match finale = Match.builder()
                    .tournament(t)
                    .player1(p1)
                    .player2(p3)
                    .numberRounds(5)
                    .matchStatus(MatchStatus.FINISHED)
                    .scheduledAt(LocalDateTime.now())
                    .playedAt(LocalDateTime.now())
                    .build();

            em.persist(m1);
            em.persist(m2);
            em.persist(finale);

            // =====================
            // 🧾 RESULTS
            // =====================
            MatchResult r1 = MatchResult.builder()
                    .match(m1)
                    .winner(p1)
                    .player1Score(2)
                    .player2Score(1)
                    .numberRounds(3)
                    .finishType(FinishType.KO)
                    .build();

            MatchResult r2 = MatchResult.builder()
                    .match(m2)
                    .winner(p3)
                    .player1Score(2)
                    .player2Score(0)
                    .numberRounds(3)
                    .finishType(FinishType.KO)
                    .build();

            MatchResult r3 = MatchResult.builder()
                    .match(finale)
                    .winner(p1)
                    .player1Score(3)
                    .player2Score(2)
                    .numberRounds(5)
                    .finishType(FinishType.TIMEOUT)
                    .build();

            em.persist(r1);
            em.persist(r2);
            em.persist(r3);

            // important pour la relation bidirectionnelle
            m1.setResult(r1);
            m2.setResult(r2);
            finale.setResult(r3);

            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}