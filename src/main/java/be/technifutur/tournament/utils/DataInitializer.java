package be.technifutur.tournament.utils;

import be.technifutur.tournament.entities.*;
import be.technifutur.tournament.enums.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DataInitializer {

    private record FighterData(String name, String style, String originCountry, String imageUrl) {}

    public void init(@Observes Startup startup) {
//        EntityManager em = null;
//        try {
//            EntityManagerFactory emf = EmfFactory.getEmf();
//            em = emf.createEntityManager();
//
//            Long count = em.createQuery("SELECT COUNT(f) FROM Fighter f", Long.class).getSingleResult();
//            if (count > 0) return;
//
//            em.getTransaction().begin();
//
//            // =====================
//            // FIGHTERS (depuis fighters.json)
//            // =====================
//            InputStream is = getClass().getClassLoader().getResourceAsStream("fighters.json");
//            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
//            Type listType = new TypeToken<List<FighterData>>() {}.getType();
//            List<FighterData> fighterDataList = new Gson().fromJson(reader, listType);
//
//            Map<String, Fighter> fighterMap = new HashMap<>();
//            for (FighterData fd : fighterDataList) {
//                Fighter f = Fighter.builder()
//                        .name(fd.name())
//                        .style(fd.style())
//                        .originCountry(fd.originCountry())
//                        .image(fd.imageUrl())
//                        .build();
//                em.persist(f);
//                fighterMap.put(fd.name(), f);
//            }
//
//            Fighter jin = fighterMap.get("Jin Kazama");
//            Fighter kazuya = fighterMap.get("Kazuya Mishima");
//            Fighter king = fighterMap.get("King");
//            Fighter nina = fighterMap.get("Nina Williams");
//
//            // =====================
//            // 👤 PLAYERS
//            // =====================
//            Player p1 = Player.builder().username("kevin").email("kevin@test.be").elo("1200").age(25).build();
//            Player p2 = Player.builder().username("laura").email("laura@test.be").elo("1250").age(23).build();
//            Player p3 = Player.builder().username("yassine").email("yassine@test.be").elo("1300").age(27).build();
//            Player p4 = Player.builder().username("sofia").email("sofia@test.be").elo("1100").age(22).build();
//
//            em.persist(p1);
//            em.persist(p2);
//            em.persist(p3);
//            em.persist(p4);
//
//            // =====================
//            // 🏆 TOURNAMENT
//            // =====================
//            Tournament t = Tournament.builder()
//                    .name("Tekken 8 Championship")
//                    .tournamentStatus(TournamentStatus.IN_PROGRESS)
//                    .startDate(LocalDateTime.now())
//                    .build();
//
//            em.persist(t);
//
//            // =====================
//            // 📝 REGISTRATIONS (fighter obligatoire)
//            // =====================
//            em.persist(Registration.builder()
//                    .player(p1)
//                    .tournament(t)
//                    .fighter(jin)
//                    .registrationStatus(RegistrationStatus.CONFIRMED)
//                    .build());
//
//            em.persist(Registration.builder()
//                    .player(p2)
//                    .tournament(t)
//                    .fighter(kazuya)
//                    .registrationStatus(RegistrationStatus.CONFIRMED)
//                    .build());
//
//            em.persist(Registration.builder()
//                    .player(p3)
//                    .tournament(t)
//                    .fighter(king)
//                    .registrationStatus(RegistrationStatus.CONFIRMED)
//                    .build());
//
//            em.persist(Registration.builder()
//                    .player(p4)
//                    .tournament(t)
//                    .fighter(nina)
//                    .registrationStatus(RegistrationStatus.CONFIRMED)
//                    .build());
//
//            // =====================
//            // ⚔️ MATCHES
//            // =====================
//            Match m1 = Match.builder()
//                    .tournament(t)
//                    .player1(p1)
//                    .player2(p2)
//                    .numberRounds(3)
//                    .matchStatus(MatchStatus.FINISHED)
//                    .scheduledAt(LocalDateTime.now().minusDays(1))
//                    .playedAt(LocalDateTime.now().minusDays(1))
//                    .build();
//
//            Match m2 = Match.builder()
//                    .tournament(t)
//                    .player1(p3)
//                    .player2(p4)
//                    .numberRounds(3)
//                    .matchStatus(MatchStatus.FINISHED)
//                    .scheduledAt(LocalDateTime.now().minusDays(1))
//                    .playedAt(LocalDateTime.now().minusDays(1))
//                    .build();
//
//            Match finale = Match.builder()
//                    .tournament(t)
//                    .player1(p1)
//                    .player2(p3)
//                    .numberRounds(5)
//                    .matchStatus(MatchStatus.FINISHED)
//                    .scheduledAt(LocalDateTime.now())
//                    .playedAt(LocalDateTime.now())
//                    .build();
//
//            em.persist(m1);
//            em.persist(m2);
//            em.persist(finale);
//
//            // =====================
//            // 🧾 RESULTS
//            // =====================
//            MatchResult r1 = MatchResult.builder()
//                    .match(m1)
//                    .winner(p1)
//                    .player1Score(2)
//                    .player2Score(1)
//                    .numberRounds(3)
//                    .finishType(FinishType.KO)
//                    .build();
//
//            MatchResult r2 = MatchResult.builder()
//                    .match(m2)
//                    .winner(p3)
//                    .player1Score(2)
//                    .player2Score(0)
//                    .numberRounds(3)
//                    .finishType(FinishType.KO)
//                    .build();
//
//            MatchResult r3 = MatchResult.builder()
//                    .match(finale)
//                    .winner(p1)
//                    .player1Score(3)
//                    .player2Score(2)
//                    .numberRounds(5)
//                    .finishType(FinishType.TIMEOUT)
//                    .build();
//
//            em.persist(r1);
//            em.persist(r2);
//            em.persist(r3);
//
//            // important pour la relation bidirectionnelle
//            m1.setResult(r1);
//            m2.setResult(r2);
//            finale.setResult(r3);
//
//            em.getTransaction().commit();
//
//        } catch (Exception e) {
//            System.err.println("[DataInitializer] ERROR: " + e.getMessage());
//            e.printStackTrace(System.err);
//            if (em != null && em.getTransaction().isActive()) {
//                em.getTransaction().rollback();
//            }
//        } finally {
//            if (em != null) em.close();
//        }
    }
}