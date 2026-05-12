package be.technifutur.tournament.utils;

import be.technifutur.tournament.dl.entity.*;
import be.technifutur.tournament.dl.enums.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

            // PLAYERS — 32 players, each with a different fighter
            List<String> fighterNames = List.of(
                    "Jin Kazama", "Kazuya Mishima", "Heihachi Mishima", "King",
                    "Nina Williams", "Paul Phoenix", "Marshall Law", "Yoshimitsu",
                    "Hwoarang", "Ling Xiaoyu", "Eddy Gordo", "Bryan Fury",
                    "Steve Fox", "Craig Marduk", "Christie Monteiro", "Asuka Kazama",
                    "Feng Wei", "Raven", "Devil Jin", "Lars Alexandersson",
                    "Alisa Bosconovitch", "Leo Kliesen", "Zafina", "Lili",
                    "Dragunov", "Anna Williams", "Lee Chaolan", "Jack",
                    "Bob", "Katarina Alves", "Shaheen", "Josie Rizal"
            );

            List<String> usernames = List.of(
                    "ShadowFist", "IronKnuckle", "VenomStrike", "MidnightRonin",
                    "BloodPhoenix", "StormBreaker", "DragonFury", "SteelTalon",
                    "VoidWalker", "ThunderClap", "CrimsonReaper", "NightHawk",
                    "ApexPredator", "SilverWolf", "BlackLotus", "PhantomBlade",
                    "TitanCrusher", "RogueSamurai", "FrostBite", "EmberKing",
                    "GhostWraith", "AshenViper", "KaijuRoar", "NeonSpecter",
                    "CobraStrike", "HyperNova", "VortexX", "ChromeFalcon",
                    "ZeroPulse", "WildKarma", "OmegaRise", "LunarBlade"
            );

            List<Player> players = new ArrayList<>();
            for (int i = 1; i <= 32; i++) {
                String username = usernames.get(i - 1);
                Player p = Player.builder()
                        .username(username)
                        .email(username.toLowerCase() + "@test.be")
                        .elo(1200)
                        .age(20 + (i % 20))
                        .fighterMain(fighterMap.get(fighterNames.get(i - 1)))
                        .build();
                em.persist(p);
                players.add(p);
            }

            // TOURNAMENT — DRAFT, no matches
            Tournament t = Tournament.builder()
                    .name("Tekken 8 Championship")
                    .status(TournamentStatus.DRAFT)
                    .startDate(LocalDateTime.now())
                    .build();
            em.persist(t);

            // REGISTRATIONS — 32 CONFIRMED
            for (Player p : players) {
                em.persist(Registration.builder()
                        .player(p)
                        .tournament(t)
                        .registrationStatus(RegistrationStatus.CONFIRMED)
                        .build());
            }

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


//    public void initB(EntityManager em) throws IOException {
//
//        em.getTransaction().begin();
//
//        try (InputStream is = getClass().getClassLoader()
//                .getResourceAsStream("scriptSqlTournament.sql")) {
//
//            if (is == null) {
//                throw new RuntimeException("SQL script not found");
//            }
//
//            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
//
//            for (String stmt : sql.split(";")) {
//                if (!stmt.trim().isEmpty()) {
//                    em.createNativeQuery(stmt).executeUpdate();
//                }
//            }
//        }
//        em.getTransaction().commit();
//
//    }


}