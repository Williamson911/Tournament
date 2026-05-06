# Bracket Endpoint Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implémenter `GET /api/tournaments/{id}/bracket` retournant un JSON `TournamentBracketDataDto` pour alimenter le bracket Angular.

**Architecture:** `BracketResource` (JAX-RS) → `BracketService` (assemblage DTO) → `MatchDAO` + `TournamentDAO`. Le fighter d'un joueur vient de `Player.fighterMain` (pas de Registration). DTOs en Java records.

**Tech Stack:** Java 21, Jakarta EE / Jersey JAX-RS, Hibernate 7 / JPA, PostgreSQL, Lombok, Jackson (sérialisation JSON).

---

## File Map

**Créer :**
- `src/main/java/be/technifutur/tournament/enums/BracketStage.java`
- `src/main/java/be/technifutur/tournament/dtos/TournamentParticipantDto.java`
- `src/main/java/be/technifutur/tournament/dtos/MatchParticipantDto.java`
- `src/main/java/be/technifutur/tournament/dtos/BracketMatchDto.java`
- `src/main/java/be/technifutur/tournament/dtos/BracketRoundDto.java`
- `src/main/java/be/technifutur/tournament/dtos/GroupStandingDto.java`
- `src/main/java/be/technifutur/tournament/dtos/TournamentGroupDto.java`
- `src/main/java/be/technifutur/tournament/dtos/TournamentBracketDataDto.java`
- `src/main/java/be/technifutur/tournament/services/BracketService.java`
- `src/main/java/be/technifutur/tournament/resources/BracketResource.java`

**Modifier :**
- `src/main/java/be/technifutur/tournament/entities/Match.java` — ajouter `bracketStage`, `roundNumber`
- `src/main/java/be/technifutur/tournament/daos/MatchDAO.java` — ajouter `findByTournamentWithPlayers`
- `src/main/java/be/technifutur/tournament/utils/DataInitializer.java` — décommenter + corriger bugs + nouveaux champs
- `src/main/resources/META-INF/persistence.xml` — `update` (Task 2) puis `validate` (Task 8)

---

## Task 1 — Enum BracketStage

**Files:**
- Create: `src/main/java/be/technifutur/tournament/enums/BracketStage.java`

- [ ] **Étape 1 : Créer l'enum**

```java
package be.technifutur.tournament.enums;

public enum BracketStage {
    WINNERS_BRACKET,
    LOSERS_BRACKET,
    GRAND_FINAL,
    GROUP_STAGE
}
```

- [ ] **Étape 2 : Vérifier la compilation**

```bash
mvn compile -q
```
Attendu : `BUILD SUCCESS`

---

## Task 2 — Mettre à jour l'entité Match + migration schema

**Files:**
- Modify: `src/main/java/be/technifutur/tournament/entities/Match.java`
- Modify: `src/main/resources/META-INF/persistence.xml`

- [ ] **Étape 1 : Ajouter les deux champs à Match**

Ajouter après le champ `bracketPosition` :

```java
@Getter @Setter
@Column(name = "bracket_stage")
@Enumerated(EnumType.STRING)
private BracketStage bracketStage;

@Getter @Setter
@Column(name = "round_number")
private Integer roundNumber;
```

L'import nécessaire : `import be.technifutur.tournament.enums.BracketStage;`

- [ ] **Étape 2 : Passer `hbm2ddl.auto` en `update` pour créer les colonnes**

Dans `persistence.xml`, changer :
```xml
<property name="hibernate.hbm2ddl.auto" value="validate"/>
```
en :
```xml
<property name="hibernate.hbm2ddl.auto" value="update"/>
```

- [ ] **Étape 3 : Compiler**

```bash
mvn compile -q
```
Attendu : `BUILD SUCCESS`

- [ ] **Étape 4 : Démarrer le serveur une fois pour créer les colonnes**

Démarrer le serveur (Tomcat/Wildfly/etc.). Hibernate crée les colonnes `bracket_stage` et `round_number` sur la table `match`. Arrêter le serveur après démarrage confirmé.

- [ ] **Étape 5 : Commit**

```bash
git add src/main/java/be/technifutur/tournament/enums/BracketStage.java
git add src/main/java/be/technifutur/tournament/entities/Match.java
git add src/main/resources/META-INF/persistence.xml
git commit -m "feat: add BracketStage enum and roundNumber/bracketStage fields to Match"
```

---

## Task 3 — Ajouter `findByTournamentWithPlayers` à MatchDAO

**Files:**
- Modify: `src/main/java/be/technifutur/tournament/daos/MatchDAO.java`

- [ ] **Étape 1 : Ajouter la méthode**

Ajouter dans `MatchDAO` :

```java
public List<Match> findByTournamentWithPlayers(int tournamentId) {
    try (var em = emf.createEntityManager()) {
        return em.createQuery(
            "SELECT m FROM Match m " +
            "JOIN FETCH m.player1 p1 JOIN FETCH p1.fighterMain " +
            "JOIN FETCH m.player2 p2 JOIN FETCH p2.fighterMain " +
            "WHERE m.tournament.id = :tid " +
            "ORDER BY m.roundNumber ASC NULLS LAST, m.id ASC",
            Match.class)
            .setParameter("tid", tournamentId)
            .getResultList();
    }
}
```

- [ ] **Étape 2 : Compiler**

```bash
mvn compile -q
```
Attendu : `BUILD SUCCESS`

- [ ] **Étape 3 : Commit**

```bash
git add src/main/java/be/technifutur/tournament/daos/MatchDAO.java
git commit -m "feat: add findByTournamentWithPlayers to MatchDAO"
```

---

## Task 4 — Couche DTOs

**Files:**
- Create: `src/main/java/be/technifutur/tournament/dtos/TournamentParticipantDto.java`
- Create: `src/main/java/be/technifutur/tournament/dtos/MatchParticipantDto.java`
- Create: `src/main/java/be/technifutur/tournament/dtos/BracketMatchDto.java`
- Create: `src/main/java/be/technifutur/tournament/dtos/BracketRoundDto.java`
- Create: `src/main/java/be/technifutur/tournament/dtos/GroupStandingDto.java`
- Create: `src/main/java/be/technifutur/tournament/dtos/TournamentGroupDto.java`
- Create: `src/main/java/be/technifutur/tournament/dtos/TournamentBracketDataDto.java`

- [ ] **Étape 1 : TournamentParticipantDto**

```java
package be.technifutur.tournament.dtos;

public record TournamentParticipantDto(
    int playerId,
    String playerName,
    String fighterName,
    String fighterImageUrl
) {}
```

- [ ] **Étape 2 : MatchParticipantDto**

```java
package be.technifutur.tournament.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MatchParticipantDto(
    int playerId,
    String playerName,
    String fighterName,
    String fighterImageUrl,
    int score,
    @JsonProperty("isWinner") boolean isWinner,
    @JsonProperty("isEliminated") boolean isEliminated
) {}
```

- [ ] **Étape 3 : BracketMatchDto**

```java
package be.technifutur.tournament.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BracketMatchDto(
    int matchId,
    MatchParticipantDto participant1,
    MatchParticipantDto participant2,
    @JsonProperty("isComplete") boolean isComplete,
    @JsonProperty("isBracketReset") boolean isBracketReset
) {}
```

- [ ] **Étape 4 : BracketRoundDto**

```java
package be.technifutur.tournament.dtos;

import java.util.List;

public record BracketRoundDto(
    int roundId,
    String label,
    List<BracketMatchDto> matches
) {}
```

- [ ] **Étape 5 : GroupStandingDto**

```java
package be.technifutur.tournament.dtos;

public record GroupStandingDto(
    int rank,
    TournamentParticipantDto participant,
    int wins,
    int losses,
    int points,
    boolean qualified
) {}
```

- [ ] **Étape 6 : TournamentGroupDto**

```java
package be.technifutur.tournament.dtos;

import java.util.List;

public record TournamentGroupDto(
    int groupId,
    String name,
    List<GroupStandingDto> standings
) {}
```

- [ ] **Étape 7 : TournamentBracketDataDto**

```java
package be.technifutur.tournament.dtos;

import java.util.List;

public record TournamentBracketDataDto(
    int tournamentId,
    String tournamentName,
    boolean hasGroupStage,
    List<TournamentGroupDto> groups,
    List<BracketRoundDto> winnersBracket,
    List<BracketRoundDto> losersBracket,
    BracketMatchDto grandFinal,
    BracketMatchDto bracketReset,
    MatchParticipantDto champion
) {}
```

- [ ] **Étape 8 : Compiler**

```bash
mvn compile -q
```
Attendu : `BUILD SUCCESS`

- [ ] **Étape 9 : Commit**

```bash
git add src/main/java/be/technifutur/tournament/dtos/
git commit -m "feat: add bracket DTOs"
```

---

## Task 5 — BracketService

**Files:**
- Create: `src/main/java/be/technifutur/tournament/services/BracketService.java`

- [ ] **Étape 1 : Créer le service**

```java
package be.technifutur.tournament.services;

import be.technifutur.tournament.daos.MatchDAO;
import be.technifutur.tournament.daos.TournamentDAO;
import be.technifutur.tournament.dtos.*;
import be.technifutur.tournament.entities.Match;
import be.technifutur.tournament.entities.Player;
import be.technifutur.tournament.enums.BracketStage;
import be.technifutur.tournament.enums.MatchStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class BracketService {

    @Inject
    private TournamentDAO tournamentDAO;

    @Inject
    private MatchDAO matchDAO;

    public TournamentBracketDataDto buildBracketData(int tournamentId) {
        var tournament = tournamentDAO.findById(tournamentId)
            .orElseThrow(() -> new NotFoundException("Tournament " + tournamentId + " not found"));

        List<Match> matches = matchDAO.findByTournamentWithPlayers(tournamentId);

        List<Match> wbMatches = matches.stream()
            .filter(m -> m.getBracketStage() == BracketStage.WINNERS_BRACKET).toList();
        List<Match> lbMatches = matches.stream()
            .filter(m -> m.getBracketStage() == BracketStage.LOSERS_BRACKET).toList();
        Optional<Match> gfMatch = matches.stream()
            .filter(m -> m.getBracketStage() == BracketStage.GRAND_FINAL).findFirst();
        List<Match> groupMatches = matches.stream()
            .filter(m -> m.getBracketStage() == BracketStage.GROUP_STAGE).toList();

        List<BracketRoundDto> winnersBracket = buildRounds(wbMatches, "WB");
        List<BracketRoundDto> losersBracket  = buildRounds(lbMatches, "LB");
        BracketMatchDto grandFinal = gfMatch.map(this::toMatchDto).orElse(null);
        MatchParticipantDto champion = gfMatch
            .filter(m -> m.getStatus() == MatchStatus.FINISHED)
            .map(m -> m.getPlayer1Score() >= m.getPlayer2Score()
                ? toParticipantDto(m.getPlayer1(), m.getPlayer1Score(), true, false)
                : toParticipantDto(m.getPlayer2(), m.getPlayer2Score(), true, false))
            .orElse(null);

        boolean hasGroupStage = !groupMatches.isEmpty();
        List<TournamentGroupDto> groups = buildGroups(groupMatches);

        return new TournamentBracketDataDto(
            tournament.getId(), tournament.getName(), hasGroupStage,
            groups, winnersBracket, losersBracket,
            grandFinal, null, champion
        );
    }

    private List<BracketRoundDto> buildRounds(List<Match> matches, String prefix) {
        if (matches.isEmpty()) return List.of();

        Map<Integer, List<Match>> byRound = matches.stream()
            .collect(Collectors.groupingBy(m -> m.getRoundNumber() != null ? m.getRoundNumber() : 0));

        int maxRound = byRound.keySet().stream().mapToInt(i -> i).max().orElse(0);

        return byRound.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> {
                int r = e.getKey();
                String label = prefix + " " + roundLabel(r, maxRound);
                List<BracketMatchDto> matchDtos = e.getValue().stream()
                    .map(this::toMatchDto).toList();
                return new BracketRoundDto(r, label, matchDtos);
            })
            .toList();
    }

    private String roundLabel(int round, int max) {
        if (round == max) return "FINALE";
        if (round == max - 1) return "DEMI-FINALE";
        return "ROUND " + round;
    }

    private BracketMatchDto toMatchDto(Match m) {
        boolean complete = m.getStatus() == MatchStatus.FINISHED;
        BracketStage stage = m.getBracketStage();
        boolean p1Wins = m.getPlayer1Score() != null && m.getPlayer2Score() != null
            && m.getPlayer1Score() > m.getPlayer2Score();

        MatchParticipantDto p1 = toParticipantDto(
            m.getPlayer1(),
            m.getPlayer1Score() != null ? m.getPlayer1Score() : 0,
            complete && p1Wins,
            complete && isEliminated(stage, !p1Wins)
        );
        MatchParticipantDto p2 = toParticipantDto(
            m.getPlayer2(),
            m.getPlayer2Score() != null ? m.getPlayer2Score() : 0,
            complete && !p1Wins,
            complete && isEliminated(stage, p1Wins)
        );
        return new BracketMatchDto(m.getId(), p1, p2, complete, false);
    }

    private boolean isEliminated(BracketStage stage, boolean isLoser) {
        if (!isLoser) return false;
        return stage == BracketStage.LOSERS_BRACKET || stage == BracketStage.GRAND_FINAL;
    }

    private MatchParticipantDto toParticipantDto(Player player, int score,
                                                  boolean isWinner, boolean isEliminated) {
        var fighter = player.getFighterMain();
        return new MatchParticipantDto(
            player.getId(), player.getUsername(),
            fighter.getName(), fighter.getImage(),
            score, isWinner, isEliminated
        );
    }

    private List<TournamentGroupDto> buildGroups(List<Match> groupMatches) {
        Map<String, List<Match>> byGroup = groupMatches.stream()
            .collect(Collectors.groupingBy(m ->
                m.getBracketPosition() != null ? m.getBracketPosition() : "GROUP_A"));

        int idCounter = 1;
        List<TournamentGroupDto> result = new ArrayList<>();
        for (var entry : byGroup.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).toList()) {
            result.add(new TournamentGroupDto(
                idCounter++, entry.getKey(), computeStandings(entry.getValue())));
        }
        return result;
    }

    private List<GroupStandingDto> computeStandings(List<Match> matches) {
        Map<Player, int[]> stats = new LinkedHashMap<>();

        for (Match m : matches) {
            stats.computeIfAbsent(m.getPlayer1(), p -> new int[3]);
            stats.computeIfAbsent(m.getPlayer2(), p -> new int[3]);
            if (m.getStatus() != MatchStatus.FINISHED) continue;

            boolean p1Wins = m.getPlayer1Score() > m.getPlayer2Score();
            Player winner = p1Wins ? m.getPlayer1() : m.getPlayer2();
            Player loser  = p1Wins ? m.getPlayer2() : m.getPlayer1();

            stats.get(winner)[0]++;      // wins
            stats.get(winner)[2] += 3;   // points
            stats.get(loser)[1]++;       // losses
        }

        var sorted = stats.entrySet().stream()
            .sorted((a, b) -> {
                int cmp = Integer.compare(b.getValue()[2], a.getValue()[2]);
                return cmp != 0 ? cmp : Integer.compare(b.getValue()[0], a.getValue()[0]);
            })
            .toList();

        List<GroupStandingDto> standings = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Player p = sorted.get(i).getKey();
            int[] s = sorted.get(i).getValue();
            var participant = new TournamentParticipantDto(
                p.getId(), p.getUsername(),
                p.getFighterMain().getName(), p.getFighterMain().getImage()
            );
            standings.add(new GroupStandingDto(i + 1, participant, s[0], s[1], s[2], i < 2));
        }
        return standings;
    }
}
```

- [ ] **Étape 2 : Compiler**

```bash
mvn compile -q
```
Attendu : `BUILD SUCCESS`

- [ ] **Étape 3 : Commit**

```bash
git add src/main/java/be/technifutur/tournament/services/BracketService.java
git commit -m "feat: add BracketService"
```

---

## Task 6 — BracketResource

**Files:**
- Create: `src/main/java/be/technifutur/tournament/resources/BracketResource.java`

- [ ] **Étape 1 : Créer la resource**

```java
package be.technifutur.tournament.resources;

import be.technifutur.tournament.services.BracketService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/tournaments")
@Produces(MediaType.APPLICATION_JSON)
public class BracketResource {

    @Inject
    private BracketService bracketService;

    @GET
    @Path("/{id}/bracket")
    public Response getBracket(@PathParam("id") int id) {
        try {
            return Response.ok(bracketService.buildBracketData(id)).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }
}
```

- [ ] **Étape 2 : Compiler**

```bash
mvn compile -q
```
Attendu : `BUILD SUCCESS`

- [ ] **Étape 3 : Commit**

```bash
git add src/main/java/be/technifutur/tournament/resources/BracketResource.java
git commit -m "feat: add BracketResource GET /tournaments/{id}/bracket"
```

---

## Task 7 — Fix DataInitializer

Le `DataInitializer` est entièrement commenté et contient plusieurs bugs. On le remplace intégralement.

**Files:**
- Modify: `src/main/java/be/technifutur/tournament/utils/DataInitializer.java`

- [ ] **Étape 1 : Remplacer le contenu du fichier**

Bugs corrigés par rapport au code commenté :
- `tournamentStatus(...)` → `status(...)` sur Tournament
- `matchStatus(...)` → `status(...)` sur Match
- `playedAt(...)` → `finishedAt(...)` sur Match
- `.fighter(...)` supprimé de Registration (champ inexistant)
- `player1Score`/`player2Score` ajoutés sur Match (non-nullable)
- `fighterMain(...)` ajouté sur chaque Player
- `MatchResult` supprimé (entité inexistante)
- `bracketStage` + `roundNumber` ajoutés sur chaque Match
- `em.getTransaction().commit()` présent et fonctionnel

```java
package be.technifutur.tournament.utils;

import be.technifutur.tournament.entities.*;
import be.technifutur.tournament.enums.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.persistence.EntityManager;

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
            em = EmfFactory.getEmf().createEntityManager();

            Long count = em.createQuery("SELECT COUNT(f) FROM Fighter f", Long.class).getSingleResult();
            if (count > 0) return;

            em.getTransaction().begin();

            // FIGHTERS
            InputStream is = getClass().getClassLoader().getResourceAsStream("fighters.json");
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

            Fighter jin    = fighterMap.get("Jin Kazama");
            Fighter kazuya = fighterMap.get("Kazuya Mishima");
            Fighter king   = fighterMap.get("King");
            Fighter nina   = fighterMap.get("Nina Williams");

            // PLAYERS
            Player p1 = Player.builder().username("kevin").email("kevin@test.be")
                .elo("1200").age(25).fighterMain(jin).build();
            Player p2 = Player.builder().username("laura").email("laura@test.be")
                .elo("1250").age(23).fighterMain(kazuya).build();
            Player p3 = Player.builder().username("yassine").email("yassine@test.be")
                .elo("1300").age(27).fighterMain(king).build();
            Player p4 = Player.builder().username("sofia").email("sofia@test.be")
                .elo("1100").age(22).fighterMain(nina).build();

            em.persist(p1);
            em.persist(p2);
            em.persist(p3);
            em.persist(p4);

            // TOURNAMENT
            Tournament t = Tournament.builder()
                .name("Tekken 8 Championship")
                .status(TournamentStatus.IN_PROGRESS)
                .startDate(LocalDateTime.now())
                .build();
            em.persist(t);

            // REGISTRATIONS
            em.persist(Registration.builder()
                .player(p1).tournament(t)
                .registrationStatus(RegistrationStatus.CONFIRMED).build());
            em.persist(Registration.builder()
                .player(p2).tournament(t)
                .registrationStatus(RegistrationStatus.CONFIRMED).build());
            em.persist(Registration.builder()
                .player(p3).tournament(t)
                .registrationStatus(RegistrationStatus.CONFIRMED).build());
            em.persist(Registration.builder()
                .player(p4).tournament(t)
                .registrationStatus(RegistrationStatus.CONFIRMED).build());

            // MATCHES — WB semi-finals
            Match m1 = Match.builder()
                .tournament(t).player1(p1).player2(p2)
                .status(MatchStatus.FINISHED)
                .numberRounds(3)
                .bracketStage(BracketStage.WINNERS_BRACKET).roundNumber(1)
                .player1Score(2).player2Score(1)
                .scheduledAt(LocalDateTime.now().minusDays(1))
                .finishedAt(LocalDateTime.now().minusDays(1))
                .build();

            Match m2 = Match.builder()
                .tournament(t).player1(p3).player2(p4)
                .status(MatchStatus.FINISHED)
                .numberRounds(3)
                .bracketStage(BracketStage.WINNERS_BRACKET).roundNumber(1)
                .player1Score(2).player2Score(0)
                .scheduledAt(LocalDateTime.now().minusDays(1))
                .finishedAt(LocalDateTime.now().minusDays(1))
                .build();

            Match finale = Match.builder()
                .tournament(t).player1(p1).player2(p3)
                .status(MatchStatus.FINISHED)
                .numberRounds(5)
                .bracketStage(BracketStage.GRAND_FINAL).roundNumber(0)
                .player1Score(3).player2Score(2)
                .scheduledAt(LocalDateTime.now())
                .finishedAt(LocalDateTime.now())
                .build();

            em.persist(m1);
            em.persist(m2);
            em.persist(finale);

            em.getTransaction().commit();

        } catch (Exception e) {
            System.err.println("[DataInitializer] ERROR: " + e.getMessage());
            e.printStackTrace(System.err);
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null) em.close();
        }
    }
}
```

- [ ] **Étape 2 : Compiler**

```bash
mvn compile -q
```
Attendu : `BUILD SUCCESS`

- [ ] **Étape 3 : Commit**

```bash
git add src/main/java/be/technifutur/tournament/utils/DataInitializer.java
git commit -m "fix: restore and correct DataInitializer with bracketStage and roundNumber"
```

---

## Task 8 — Repasser en `validate` + test end-to-end

**Files:**
- Modify: `src/main/resources/META-INF/persistence.xml`

- [ ] **Étape 1 : Repasser `hbm2ddl.auto` en `validate`**

```xml
<property name="hibernate.hbm2ddl.auto" value="validate"/>
```

- [ ] **Étape 2 : Vider la base et redémarrer**

Se connecter à PostgreSQL et tronquer les tables pour relancer le seed proprement :

```sql
TRUNCATE match, registration, tournament, player, fighter RESTART IDENTITY CASCADE;
```

Redémarrer le serveur. Le `DataInitializer` insère les données de seed.

- [ ] **Étape 3 : Tester le endpoint**

```bash
curl -s http://localhost:8080/api/tournaments/1/bracket | python -m json.tool
```
(ou via Postman / Bruno)

Vérifier dans la réponse :
- `tournamentName` = `"Tekken 8 Championship"`
- `winnersBracket` contient 1 ronde (`"WB ROUND 1"`) avec 2 matchs
- `grandFinal` contient le match kevin vs yassine, `isComplete: true`
- `champion.playerName` = `"kevin"`
- `hasGroupStage` = `false`
- `losersBracket` = `[]`

- [ ] **Étape 4 : Commit final**

```bash
git add src/main/resources/META-INF/persistence.xml
git commit -m "feat: bracket endpoint complete - switch back to validate mode"
```

- [ ] **Étape 5 : Retirer le fallback mock du BracketPageComponent Angular**

Dans `frontend/src/app/bracket/bracket-page/bracket-page.component.ts`, dans `ngOnInit` :

Remplacer :
```typescript
error: () => {
  // TODO: retirer le fallback mock une fois le backend prêt
  this.data.set(MOCK_DATA);
  this.loading.set(false);
}
```
par :
```typescript
error: () => {
  this.error.set('Impossible de charger le bracket.');
  this.loading.set(false);
}
```

Et supprimer la constante `MOCK_DATA` et les constantes `*_IMG` en haut du fichier (plus besoin).

```bash
git add frontend/src/app/bracket/bracket-page/bracket-page.component.ts
git commit -m "chore: remove mock fallback from BracketPageComponent"
```
