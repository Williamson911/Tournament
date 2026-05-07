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
        EntityManager em = null;

        try {
            EntityManagerFactory emf = EmfFactory.getEmf();
            em = emf.createEntityManager();

            // ❌ Skip si déjà data
            Long count = em.createQuery("SELECT COUNT(f) FROM Fighter f", Long.class).getSingleResult();
            if (count > 0) return;

            em.getTransaction().begin();

            // =====================
            // 🥊 FIGHTERS
            // =====================
            InputStream is = getClass().getClassLoader().getResourceAsStream("fighters.json");
            if (is == null) throw new RuntimeException("fighters.json not found");

            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            Type listType = new TypeToken<List<FighterData>>() {}.getType();
            List<FighterData> fighterDataList = new Gson().fromJson(reader, listType);

            Map<String, Fighter> fighterMap = new HashMap<>();

            for (FighterData fd : fighterDataList) {
                Fighter f = Fighter.builder()
                        .name(fd.name())
                        .style(fd.style())
                        .originCountry(fd.originCountry())
                        .image(fd.imageUrl())
                        .build();

                em.persist(f);
                fighterMap.put(fd.name(), f);
            }

            // =====================
            // 👤 PLAYERS
            // =====================
            Player p1 = Player.builder()
                    .username("kevin")
                    .email("kevin@test.be")
                    .elo("1200")
                    .age(25)
                    .fighterMain(fighterMap.get("Jin Kazama"))
                    .build();

            Player p2 = Player.builder()
                    .username("laura")
                    .email("laura@test.be")
                    .elo("1250")
                    .age(23)
                    .fighterMain(fighterMap.get("Kazuya Mishima"))
                    .build();

            Player p3 = Player.builder()
                    .username("yassine")
                    .email("yassine@test.be")
                    .elo("1300")
                    .age(27)
                    .fighterMain(fighterMap.get("King"))
                    .build();

            Player p4 = Player.builder()
                    .username("sofia")
                    .email("sofia@test.be")
                    .elo("1100")
                    .age(22)
                    .fighterMain(fighterMap.get("Heihachi Mishima"))
                    .build();

            em.persist(p1);
            em.persist(p2);
            em.persist(p3);
            em.persist(p4);

            // =====================
            // 🏆 TOURNAMENT
            // =====================
            Tournament t = Tournament.builder()
                    .name("Tekken 8 Championship")
                    .status(TournamentStatus.IN_PROGRESS)
                    .startDate(LocalDateTime.now())
                    .build();

            em.persist(t);

            // =====================
            // 📝 REGISTRATIONS
            // =====================
            for (Player p : List.of(p1, p2, p3, p4)) {
                em.persist(Registration.builder()
                        .player(p)
                        .tournament(t)
                        .registrationStatus(RegistrationStatus.CONFIRMED)
                        .build());
            }

            // =====================
            // ⚔️ MATCHES
            // =====================
            Match m1 = Match.builder()
                    .tournament(t)
                    .player1(p1)
                    .player2(p2)
                    .numberRounds(3)
                    .status(MatchStatus.FINISHED)
                    .bracketStage(BracketStage.WINNERS_BRACKET)
                    .roundNumber(1)
                    .bracketPosition("W11")
                    .player1Score(2)
                    .player2Score(1)
                    .scheduledAt(LocalDateTime.now().minusDays(1))
                    .startedAt(LocalDateTime.now().minusDays(1))
                    .finishedAt(LocalDateTime.now().minusDays(1))
                    .build();

            Match m2 = Match.builder()
                    .tournament(t)
                    .player1(p3)
                    .player2(p4)
                    .numberRounds(3)
                    .status(MatchStatus.FINISHED)
                    .bracketStage(BracketStage.WINNERS_BRACKET)
                    .roundNumber(1)
                    .bracketPosition("W12")
                    .player1Score(2)
                    .player2Score(0)
                    .scheduledAt(LocalDateTime.now().minusDays(1))
                    .startedAt(LocalDateTime.now().minusDays(1))
                    .finishedAt(LocalDateTime.now().minusDays(1))
                    .build();

            Match finale = Match.builder()
                    .tournament(t)
                    .player1(p1)
                    .player2(p3)
                    .numberRounds(5)
                    .status(MatchStatus.FINISHED)
                    .bracketStage(BracketStage.GRAND_FINAL)
                    .roundNumber(0)
                    .bracketPosition("GF1")
                    .player1Score(3)
                    .player2Score(2)
                    .scheduledAt(LocalDateTime.now())
                    .startedAt(LocalDateTime.now())
                    .finishedAt(LocalDateTime.now())
                    .build();

            em.persist(m1);
            em.persist(m2);
            em.persist(finale);

            em.getTransaction().commit();

        } catch (Exception e) {
            System.err.println("[DataInitializer] ERROR: " + e.getMessage());
            e.printStackTrace();

            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null) em.close();
        }
    }
}