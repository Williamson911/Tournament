# Group Stage + 32-Player Bracket Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a two-phase flow where group stage auto-simulation selects 16 qualifiers from 32 registered players, who are then placed into the existing double-elimination bracket via two action buttons on the bracket page.

**Architecture:** Backend adds `GROUP_STAGE_COMPLETE` status, `QUALIFIED` registration status, `hasGroupStage` entity field, a new `GroupStageService` (pure computation), a new `launchGroupStage` endpoint, and modifies `generateBracket` to handle both DRAFT and GROUP_STAGE_COMPLETE paths. Frontend adds two conditional buttons driven by `data().status`.

**Tech Stack:** Quarkus/Java backend (JPA/JAX-RS/JUnit 5) · Angular 17+ frontend (signals, standalone components)

---

## File Map

**Backend — create:**
- `src/main/java/be/technifutur/tournament/services/GroupStageService.java`
- `src/test/java/be/technifutur/tournament/services/GroupStageServiceTest.java`

**Backend — modify:**
- `src/main/java/be/technifutur/tournament/enums/TournamentStatus.java`
- `src/main/java/be/technifutur/tournament/enums/RegistrationStatus.java`
- `src/main/java/be/technifutur/tournament/entities/Tournament.java`
- `src/main/java/be/technifutur/tournament/dtos/TournamentBracketDataDto.java`
- `src/main/java/be/technifutur/tournament/daos/RegistrationDAO.java`
- `src/main/java/be/technifutur/tournament/services/TournamentService.java`
- `src/main/java/be/technifutur/tournament/services/BracketService.java`
- `src/main/java/be/technifutur/tournament/resources/TournamentResource.java`
- `src/main/java/be/technifutur/tournament/utils/DataInitializer.java`

**Frontend — modify:**
- `frontend/src/app/models/bracket.models.ts`
- `frontend/src/app/services/tournament-bracket.service.ts`
- `frontend/src/app/bracket/bracket-page/bracket-page.component.ts`
- `frontend/src/app/bracket/bracket-page/bracket-page.component.html`
- `frontend/src/app/bracket/bracket-page/bracket-page.component.scss`

---

### Task 1: Enum values

**Files:**
- Modify: `src/main/java/be/technifutur/tournament/enums/TournamentStatus.java`
- Modify: `src/main/java/be/technifutur/tournament/enums/RegistrationStatus.java`

- [ ] **Step 1: Update TournamentStatus**

Replace the entire file:

```java
package be.technifutur.tournament.enums;

public enum TournamentStatus {
    DRAFT,
    OPEN,
    GROUP_STAGE_COMPLETE,
    IN_PROGRESS,
    FINISHED
}
```

- [ ] **Step 2: Update RegistrationStatus**

Replace the entire file:

```java
package be.technifutur.tournament.enums;

public enum RegistrationStatus {
    PENDING,
    CONFIRMED,
    QUALIFIED,
    DISQUALIFIED
}
```

- [ ] **Step 3: Verify existing tests still pass**

Run: `mvn test`
Expected: All existing tests pass (additive enum values, nothing breaks).

- [ ] **Step 4: Commit**

```bash
git add src/main/java/be/technifutur/tournament/enums/TournamentStatus.java \
        src/main/java/be/technifutur/tournament/enums/RegistrationStatus.java
git commit -m "feat: add GROUP_STAGE_COMPLETE status and QUALIFIED registration status"
```

---

### Task 2: Tournament entity — add hasGroupStage field

**Files:**
- Modify: `src/main/java/be/technifutur/tournament/entities/Tournament.java`

- [ ] **Step 1: Add hasGroupStage field**

Replace the entire file:

```java
package be.technifutur.tournament.entities;

import be.technifutur.tournament.enums.TournamentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Tournament {

    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TournamentStatus status;

    @Getter @Setter
    @Column(nullable = false)
    private String name;

    @Getter @Setter
    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Getter @Setter
    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Getter @Setter
    @Column(name = "has_group_stage")
    private boolean hasGroupStage;
}
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile`
Expected: BUILD SUCCESS. Hibernate will auto-add the `has_group_stage` column on next startup (schema auto-update is configured).

- [ ] **Step 3: Commit**

```bash
git add src/main/java/be/technifutur/tournament/entities/Tournament.java
git commit -m "feat: add hasGroupStage field to Tournament entity"
```

---

### Task 3: TournamentBracketDataDto + BracketService fix

Add `status` to the DTO record, then fix `BracketService` in the same task so compilation stays clean.

**Files:**
- Modify: `src/main/java/be/technifutur/tournament/dtos/TournamentBracketDataDto.java`
- Modify: `src/main/java/be/technifutur/tournament/services/BracketService.java`

- [ ] **Step 1: Add status field to TournamentBracketDataDto**

Replace the entire file:

```java
package be.technifutur.tournament.dtos;

import java.util.List;

public record TournamentBracketDataDto(
    int tournamentId,
    String tournamentName,
    String status,
    boolean hasGroupStage,
    List<TournamentGroupDto> groups,
    List<BracketRoundDto> winnersBracket,
    List<BracketRoundDto> losersBracket,
    BracketMatchDto grandFinal,
    MatchParticipantDto champion
) {}
```

- [ ] **Step 2: Fix BracketService — use tournament.isHasGroupStage() and pass status**

Two changes in `buildBracketData`:
1. `boolean hasGroupStage = !groupMatches.isEmpty();` → `boolean hasGroupStage = tournament.isHasGroupStage();`
2. DTO constructor call gets `tournament.getStatus().name()` as the new third argument

Replace the `buildBracketData` method:

```java
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
            .map(m -> m.getPlayer1Score() > m.getPlayer2Score()
                ? toParticipantDto(m.getPlayer1(), m.getPlayer1Score(), true, false)
                : toParticipantDto(m.getPlayer2(), m.getPlayer2Score(), true, false))
            .orElse(null);

        boolean hasGroupStage = tournament.isHasGroupStage();
        List<TournamentGroupDto> groups = buildGroups(groupMatches);

        return new TournamentBracketDataDto(
            tournament.getId(), tournament.getName(), tournament.getStatus().name(), hasGroupStage,
            groups, winnersBracket, losersBracket,
            grandFinal, champion
        );
    }
```

- [ ] **Step 3: Verify tests pass**

Run: `mvn test`
Expected: All existing tests pass.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/be/technifutur/tournament/dtos/TournamentBracketDataDto.java \
        src/main/java/be/technifutur/tournament/services/BracketService.java
git commit -m "feat: add status to TournamentBracketDataDto and fix hasGroupStage derivation in BracketService"
```

---

### Task 4: RegistrationDAO — new queries

Add a parameterized status query and a bulk status update.

**Files:**
- Modify: `src/main/java/be/technifutur/tournament/daos/RegistrationDAO.java`

- [ ] **Step 1: Add import and two new methods**

Add `import java.util.Set;` to the existing imports at the top.

Add these two methods after the existing `findByTournamentWithStatus` method:

```java
    public List<Registration> findByTournamentAndStatus(int tournamentId, RegistrationStatus status) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            return em.createQuery(
                            "SELECT r FROM Registration r WHERE r.tournament.id = :tid " +
                                    "AND r.registrationStatus = :status",
                            Registration.class)
                    .setParameter("tid", tournamentId)
                    .setParameter("status", status)
                    .getResultList();
        }
    }

    public void updateStatusForPlayers(int tournamentId, Set<Integer> playerIds, RegistrationStatus status) {
        try (EntityManager em = emfProvider.get().createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery(
                            "UPDATE Registration r SET r.registrationStatus = :status " +
                                    "WHERE r.tournament.id = :tid AND r.player.id IN :playerIds")
                    .setParameter("status", status)
                    .setParameter("tid", tournamentId)
                    .setParameter("playerIds", playerIds)
                    .executeUpdate();
            em.getTransaction().commit();
        }
    }
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/be/technifutur/tournament/daos/RegistrationDAO.java
git commit -m "feat: add findByTournamentAndStatus and updateStatusForPlayers to RegistrationDAO"
```

---

### Task 5: GroupStageService — new pure computation service

**Files:**
- Create: `src/main/java/be/technifutur/tournament/services/GroupStageService.java`
- Test: `src/test/java/be/technifutur/tournament/services/GroupStageServiceTest.java`

- [ ] **Step 1: Write the failing tests**

Create `src/test/java/be/technifutur/tournament/services/GroupStageServiceTest.java`:

```java
package be.technifutur.tournament.services;

import be.technifutur.tournament.entities.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class GroupStageServiceTest {

    private GroupStageService service;

    @BeforeEach
    void setUp() {
        service = new GroupStageService();
    }

    private List<Player> players(int count) {
        return IntStream.rangeClosed(1, count)
            .mapToObj(i -> Player.builder().username("p" + i).build())
            .toList();
    }

    @Test
    void selectQualifiers_32_returns_16() {
        assertEquals(16, service.selectQualifiers(players(32)).size());
    }

    @Test
    void selectQualifiers_8_returns_4() {
        assertEquals(4, service.selectQualifiers(players(8)).size());
    }

    @Test
    void selectQualifiers_all_from_input() {
        List<Player> input = players(16);
        List<Player> qualifiers = service.selectQualifiers(input);
        assertTrue(input.containsAll(qualifiers));
    }

    @Test
    void selectQualifiers_no_duplicates() {
        List<Player> qualifiers = service.selectQualifiers(players(32));
        long unique = qualifiers.stream().distinct().count();
        assertEquals(16, unique);
    }
}
```

- [ ] **Step 2: Run test to confirm it fails**

Run: `mvn test -Dtest=GroupStageServiceTest`
Expected: Compilation error — GroupStageService does not exist yet.

- [ ] **Step 3: Implement GroupStageService**

Create `src/main/java/be/technifutur/tournament/services/GroupStageService.java`:

```java
package be.technifutur.tournament.services;

import be.technifutur.tournament.entities.Player;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class GroupStageService {

    public List<Player> selectQualifiers(List<Player> players) {
        List<Player> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled);
        List<Player> qualifiers = new ArrayList<>();
        for (int i = 0; i < shuffled.size(); i += 4) {
            List<Player> group = new ArrayList<>(shuffled.subList(i, i + 4));
            Collections.shuffle(group);
            qualifiers.add(group.get(0));
            qualifiers.add(group.get(1));
        }
        return qualifiers;
    }
}
```

- [ ] **Step 4: Run tests to confirm they pass**

Run: `mvn test -Dtest=GroupStageServiceTest`
Expected: 4 tests, all PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/be/technifutur/tournament/services/GroupStageService.java \
        src/test/java/be/technifutur/tournament/services/GroupStageServiceTest.java
git commit -m "feat: add GroupStageService with random group qualifier selection"
```

---

### Task 6: TournamentService — launchGroupStage and updated generateBracket

**Files:**
- Modify: `src/main/java/be/technifutur/tournament/services/TournamentService.java`

- [ ] **Step 1: Rewrite TournamentService**

Replace the entire file:

```java
package be.technifutur.tournament.services;

import be.technifutur.tournament.daos.MatchDAO;
import be.technifutur.tournament.daos.PlayerDao;
import be.technifutur.tournament.daos.RegistrationDAO;
import be.technifutur.tournament.daos.TournamentDAO;
import be.technifutur.tournament.dtos.TournamentBracketDataDto;
import be.technifutur.tournament.entities.Player;
import be.technifutur.tournament.entities.Registration;
import be.technifutur.tournament.entities.Tournament;
import be.technifutur.tournament.enums.RegistrationStatus;
import be.technifutur.tournament.enums.TournamentStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class TournamentService {

    @Inject TournamentDAO tournamentDAO;
    @Inject RegistrationDAO registrationDAO;
    @Inject PlayerDao playerDAO;
    @Inject MatchDAO matchDAO;
    @Inject BracketGenerationService generationService;
    @Inject BracketService bracketService;
    @Inject GroupStageService groupStageService;

    public List<Tournament> findAll() {
        return tournamentDAO.findAll();
    }

    public Tournament findById(int id) {
        return tournamentDAO.findById(id)
            .orElseThrow(() -> new NotFoundException("Tournament not found"));
    }

    public Tournament update(int id, String name, LocalDateTime startDate) {
        Tournament t = tournamentDAO.findById(id)
            .orElseThrow(() -> new NotFoundException("Tournament not found"));
        t.setName(name);
        t.setStartDate(startDate);
        tournamentDAO.update(t);
        return t;
    }

    public void delete(int id) {
        tournamentDAO.findById(id)
            .orElseThrow(() -> new NotFoundException("Tournament not found"));
        tournamentDAO.delete(id);
    }

    public Tournament create(String name, LocalDateTime startDate) {
        Tournament t = Tournament.builder()
            .name(name).startDate(startDate).status(TournamentStatus.DRAFT).build();
        tournamentDAO.save(t);
        return t;
    }

    public Registration register(int tournamentId, int playerId) {
        Tournament t = tournamentDAO.findById(tournamentId)
            .orElseThrow(() -> new NotFoundException("Tournament not found"));
        if (t.getStatus() != TournamentStatus.DRAFT && t.getStatus() != TournamentStatus.OPEN)
            throw new BadRequestException("Registration is closed for this tournament");
        if (registrationDAO.existsByPlayerAndTournament(playerId, tournamentId))
            throw new WebApplicationException("Player already registered", 409);
        Player player = playerDAO.findById(playerId)
            .orElseThrow(() -> new NotFoundException("Player not found"));
        Registration reg = Registration.builder()
            .tournament(t).player(player).registrationStatus(RegistrationStatus.CONFIRMED).build();
        registrationDAO.save(reg);
        return reg;
    }

    public void unregister(int tournamentId, int playerId) {
        Registration reg = registrationDAO.findByPlayerAndTournament(playerId, tournamentId)
            .orElseThrow(() -> new NotFoundException("Registration not found"));
        registrationDAO.delete(reg.getId());
    }

    public List<Player> launchGroupStage(int tournamentId) {
        Tournament t = tournamentDAO.findById(tournamentId)
            .orElseThrow(() -> new NotFoundException("Tournament not found"));
        if (t.getStatus() != TournamentStatus.DRAFT)
            throw new BadRequestException("Tournament must be in DRAFT status");

        List<Registration> registrations = registrationDAO.findByTournamentWithStatus(tournamentId);
        int n = registrations.size();
        if (n % 4 != 0)
            throw new BadRequestException("Player count must be divisible by 4");
        int half = n / 2;
        if (half < 2 || (half & (half - 1)) != 0)
            throw new BadRequestException("N/2 must be a power of 2 (result must be 4, 8, 16...)");

        List<Player> players = registrations.stream().map(Registration::getPlayer).toList();
        List<Player> qualifiers = groupStageService.selectQualifiers(players);

        Set<Integer> qualifierIds = qualifiers.stream().map(Player::getId).collect(Collectors.toSet());
        registrationDAO.updateStatusForPlayers(tournamentId, qualifierIds, RegistrationStatus.QUALIFIED);

        t.setHasGroupStage(true);
        t.setStatus(TournamentStatus.GROUP_STAGE_COMPLETE);
        tournamentDAO.update(t);

        return qualifiers;
    }

    public TournamentBracketDataDto generateBracket(int tournamentId) {
        Tournament t = tournamentDAO.findById(tournamentId)
            .orElseThrow(() -> new NotFoundException("Tournament not found"));

        List<Registration> registrations;
        if (t.getStatus() == TournamentStatus.GROUP_STAGE_COMPLETE) {
            registrations = registrationDAO.findByTournamentAndStatus(tournamentId, RegistrationStatus.QUALIFIED);
        } else if (t.getStatus() == TournamentStatus.DRAFT) {
            registrations = registrationDAO.findByTournamentWithStatus(tournamentId);
        } else {
            throw new BadRequestException("Tournament must be in DRAFT or GROUP_STAGE_COMPLETE status");
        }

        int n = registrations.size();
        if (n < 4 || (n & (n - 1)) != 0)
            throw new BadRequestException("Player count must be a power of 2 (4, 8, 16...)");

        List<Player> players = registrations.stream().map(Registration::getPlayer).toList();
        generationService.generate(t, players).forEach(matchDAO::save);

        t.setStatus(TournamentStatus.IN_PROGRESS);
        tournamentDAO.update(t);

        return bracketService.buildBracketData(tournamentId);
    }

    public Tournament updateStatus(int tournamentId, TournamentStatus status) {
        Tournament t = tournamentDAO.findById(tournamentId)
            .orElseThrow(() -> new NotFoundException("Tournament not found"));
        t.setStatus(status);
        tournamentDAO.update(t);
        return t;
    }
}
```

- [ ] **Step 2: Verify tests pass**

Run: `mvn test`
Expected: All tests pass.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/be/technifutur/tournament/services/TournamentService.java
git commit -m "feat: add launchGroupStage and update generateBracket for group stage flow"
```

---

### Task 7: TournamentResource — new endpoint

**Files:**
- Modify: `src/main/java/be/technifutur/tournament/resources/TournamentResource.java`

- [ ] **Step 1: Add launch-group-stage endpoint**

Add these imports if not already present:

```java
import be.technifutur.tournament.entities.Player;
import java.util.List;
```

Add after the `generateBracket` method (after line 83), before `updateStatus`:

```java
    @POST
    @Path("/{id}/launch-group-stage")
    @Operation(summary = "Run group stage and select qualifiers")
    public Response launchGroupStage(@PathParam("id") int id) {
        List<Player> qualifiers = tournamentService.launchGroupStage(id);
        List<Integer> qualifierIds = qualifiers.stream().map(Player::getId).toList();
        return Response.ok(qualifierIds).build();
    }
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/be/technifutur/tournament/resources/TournamentResource.java
git commit -m "feat: add POST /tournaments/{id}/launch-group-stage endpoint"
```

---

### Task 8: DataInitializer — rewrite for 32 players in DRAFT

**Files:**
- Modify: `src/main/java/be/technifutur/tournament/utils/DataInitializer.java`

- [ ] **Step 1: Rewrite DataInitializer**

Replace the entire file:

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
import jakarta.persistence.EntityManagerFactory;

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

            Long count = em.createQuery("SELECT COUNT(f) FROM Fighter f", Long.class).getSingleResult();
            if (count > 0) return;

            em.getTransaction().begin();

            // FIGHTERS — load from JSON
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

            List<Player> players = new ArrayList<>();
            for (int i = 1; i <= 32; i++) {
                Player p = Player.builder()
                        .username("player" + i)
                        .email("player" + i + "@test.be")
                        .elo("1200")
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
}
```

- [ ] **Step 2: Reset the database**

The DataInitializer skips if Fighter records already exist. Drop the existing database file so it re-runs with 32 players. Find the `.mv.db` file (typically in the project root or configured path in `application.properties`) and delete it, then restart the backend.

- [ ] **Step 3: Start the backend and verify data**

Start Quarkus: `mvn quarkus:dev`
Verify: `GET /api/tournaments/1` returns the tournament with `status: "DRAFT"`.
Verify: `GET /api/tournaments/1/bracket` returns `status: "DRAFT"`, `hasGroupStage: false`, empty `winnersBracket`.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/be/technifutur/tournament/utils/DataInitializer.java
git commit -m "feat: rewrite DataInitializer for 32 players in DRAFT tournament"
```

---

### Task 9: Frontend — model and service

**Files:**
- Modify: `frontend/src/app/models/bracket.models.ts`
- Modify: `frontend/src/app/services/tournament-bracket.service.ts`

- [ ] **Step 1: Add status field to TournamentBracketData**

In `frontend/src/app/models/bracket.models.ts`, add `status: string` after `tournamentName` in the `TournamentBracketData` interface:

```typescript
export interface TournamentBracketData {
  tournamentId: number;
  tournamentName: string;
  status: string;
  hasGroupStage: boolean;
  groups: TournamentGroup[];
  winnersBracket: BracketRound[];
  losersBracket: BracketRound[];
  grandFinal: BracketMatch | null;
  champion: MatchParticipant | null;
}
```

(All other interfaces in the file remain unchanged.)

- [ ] **Step 2: Add launchGroupStage and generateBracket to TournamentBracketService**

Replace `frontend/src/app/services/tournament-bracket.service.ts` entirely:

```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TournamentBracketData } from '../models/bracket.models';

@Injectable({ providedIn: 'root' })
export class TournamentBracketService {
  private http = inject(HttpClient);

  getBracket(tournamentId: number): Observable<TournamentBracketData> {
    return this.http.get<TournamentBracketData>(`/api/tournaments/${tournamentId}/bracket`);
  }

  launchGroupStage(tournamentId: number): Observable<any> {
    return this.http.post(`/api/tournaments/${tournamentId}/launch-group-stage`, {});
  }

  generateBracket(tournamentId: number): Observable<any> {
    return this.http.post(`/api/tournaments/${tournamentId}/generate-bracket`, {});
  }
}
```

- [ ] **Step 3: Verify TypeScript compilation**

Run from `frontend/`: `ng build --configuration=development`
Expected: No TypeScript errors, build succeeds.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/app/models/bracket.models.ts \
        frontend/src/app/services/tournament-bracket.service.ts
git commit -m "feat: add status to TournamentBracketData model and group stage service methods"
```

---

### Task 10: Frontend — bracket-page action buttons

**Files:**
- Modify: `frontend/src/app/bracket/bracket-page/bracket-page.component.ts`
- Modify: `frontend/src/app/bracket/bracket-page/bracket-page.component.html`
- Modify: `frontend/src/app/bracket/bracket-page/bracket-page.component.scss`

- [ ] **Step 1: Update bracket-page.component.ts**

Replace the entire file:

```typescript
import { AfterViewInit, Component, ElementRef, inject, signal, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TournamentBracketService } from '../../services/tournament-bracket.service';
import { TournamentBracketData } from '../../models/bracket.models';
import { BracketTitleComponent } from '../bracket-title/bracket-title.component';
import { StageTabsComponent, StageTab } from '../stage-tabs/stage-tabs.component';
import { GroupStageComponent } from '../group-stage/group-stage.component';
import { BracketLayoutComponent } from '../bracket-layout/bracket-layout.component';
import { BracketConnectorComponent } from '../bracket-connector/bracket-connector.component';
import { GrandFinalComponent } from '../grand-final/grand-final.component';
import { ChampionCardComponent } from '../champion-card/champion-card.component';

@Component({
  selector: 'app-bracket-page',
  imports: [
    BracketTitleComponent, StageTabsComponent, GroupStageComponent,
    BracketLayoutComponent, BracketConnectorComponent,
    GrandFinalComponent, ChampionCardComponent
  ],
  templateUrl: './bracket-page.component.html',
  styleUrl: './bracket-page.component.scss'
})
export class BracketPageComponent implements OnInit, AfterViewInit {
  private route = inject(ActivatedRoute);
  private service = inject(TournamentBracketService);

  @ViewChild('bracketLayoutRef', { read: ElementRef }) private bracketLayoutRef?: ElementRef;
  @ViewChild('grandFinalRef', { read: ElementRef }) private grandFinalRef?: ElementRef;

  data = signal<TournamentBracketData | null>(null);
  activeTab = signal<StageTab>('bracket');
  loading = signal(true);
  error = signal<string | null>(null);
  launching = signal(false);
  generating = signal(false);
  gfSvgH = signal<number | null>(null);
  gfTopLineY = signal<number | null>(null);
  gfBottomLineY = signal<number | null>(null);
  gfMarginTop = signal<number | null>(null);

  private tournamentId = 0;

  ngOnInit(): void {
    this.tournamentId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadBracket();
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.measureGfConnector());
  }

  launchGroupStage(): void {
    this.launching.set(true);
    this.service.launchGroupStage(this.tournamentId).subscribe({
      next: () => this.loadBracket(),
      error: () => this.launching.set(false)
    });
  }

  generateBracket(): void {
    this.generating.set(true);
    this.service.generateBracket(this.tournamentId).subscribe({
      next: () => this.loadBracket(),
      error: () => this.generating.set(false)
    });
  }

  private loadBracket(): void {
    this.service.getBracket(this.tournamentId).subscribe({
      next: d => {
        this.data.set(d);
        this.loading.set(false);
        this.launching.set(false);
        this.generating.set(false);
        if (d.hasGroupStage && d.status !== 'IN_PROGRESS') {
          this.activeTab.set('groups');
        }
        setTimeout(() => this.measureGfConnector());
      },
      error: () => {
        this.error.set('Impossible de charger le bracket.');
        this.loading.set(false);
        this.launching.set(false);
        this.generating.set(false);
      }
    });
  }

  private measureGfConnector(): void {
    const host = this.bracketLayoutRef?.nativeElement as HTMLElement | undefined;
    if (!host) return;
    const hostRect = host.getBoundingClientRect();
    if (!hostRect.height) return;

    const wbRounds = host.querySelectorAll('app-winners-bracket app-bracket-round');
    const lbRounds = host.querySelectorAll('app-losers-bracket app-bracket-round');
    const lastWbRound = wbRounds[wbRounds.length - 1] as HTMLElement | undefined;
    const lastLbRound = lbRounds[lbRounds.length - 1] as HTMLElement | undefined;
    if (!lastWbRound || !lastLbRound) return;

    const wbMatches = lastWbRound.querySelectorAll('app-bracket-match');
    const lbMatches = lastLbRound.querySelectorAll('app-bracket-match');
    const wbEl = (wbMatches[wbMatches.length - 1] as HTMLElement | undefined) ?? lastWbRound;
    const lbEl = (lbMatches[lbMatches.length - 1] as HTMLElement | undefined) ?? lastLbRound;

    const wbRect = wbEl.getBoundingClientRect();
    const lbRect = lbEl.getBoundingClientRect();
    const h = hostRect.height;
    this.gfSvgH.set(h);
    const topY = (wbRect.top + wbRect.bottom) / 2 - hostRect.top;
    const botY = (lbRect.top + lbRect.bottom) / 2 - hostRect.top;
    this.gfTopLineY.set(topY);
    this.gfBottomLineY.set(botY);

    const mid = (topY + botY) / 2;
    const gfHost = this.grandFinalRef?.nativeElement as HTMLElement | undefined;
    if (gfHost) {
      const vsDivider = gfHost.querySelector('.vs-divider') as HTMLElement | null;
      if (vsDivider) {
        const vsRect = vsDivider.getBoundingClientRect();
        const gfRect = gfHost.getBoundingClientRect();
        const vsOffsetInGf = (vsRect.top + vsRect.bottom) / 2 - gfRect.top;
        this.gfMarginTop.set(Math.max(0, mid - vsOffsetInGf));
      }
    }
  }

  onTabChange(tab: StageTab): void { this.activeTab.set(tab); }
}
```

- [ ] **Step 2: Update bracket-page.component.html**

Replace the entire file:

```html
<div class="page">
  @if (loading()) {
    <div class="state-msg">Chargement…</div>
  } @else if (error()) {
    <div class="state-msg error">{{ error() }}</div>
  } @else {
    @let d = data()!;
    <app-bracket-title [title]="d.tournamentName" subtitle="Iron Fist Tournament · Grand Prix" />

    @if (d.hasGroupStage) {
      <app-stage-tabs [activeTab]="activeTab()" (tabChange)="onTabChange($event)" />
    }

    @if (!d.hasGroupStage || activeTab() === 'bracket') {
      <div class="bracket-view">
        @if (d.status === 'DRAFT') {
          <button class="action-btn" [disabled]="launching()" (click)="launchGroupStage()">
            {{ launching() ? 'En cours…' : 'Lancer les poules' }}
          </button>
        }
        @if (d.status === 'GROUP_STAGE_COMPLETE') {
          <button class="action-btn" [disabled]="generating()" (click)="generateBracket()">
            {{ generating() ? 'En cours…' : 'Générer le bracket' }}
          </button>
        }

        <app-bracket-layout #bracketLayoutRef
          [winnersBracket]="d.winnersBracket"
          [losersBracket]="d.losersBracket" />

        @if (d.grandFinal) {
          <app-bracket-connector [topLineY]="gfTopLineY()" [bottomLineY]="gfBottomLineY()" [svgH]="gfSvgH()" />
          <app-grand-final #grandFinalRef [grandFinal]="d.grandFinal"
                           [style.margin-top.px]="gfMarginTop()" />
        }

        @if (d.champion) {
          <app-bracket-connector [matchCount]="1" />
          <app-champion-card [champion]="d.champion" />
        }
      </div>
    }

    @if (d.hasGroupStage && activeTab() === 'groups') {
      <app-group-stage [groups]="d.groups" />
    }
  }
</div>
```

- [ ] **Step 3: Add action-btn styles to bracket-page.component.scss**

Add after the existing `.state-msg` block:

```scss
.action-btn {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 16px;
  letter-spacing: 4px;
  color: rgba(220, 80, 80, 0.9);
  background: rgba(10, 0, 0, 0.7);
  border: 1px solid rgba(180, 20, 20, 0.6);
  padding: 10px 32px;
  cursor: pointer;
  align-self: center;

  &:hover:not(:disabled) {
    background: rgba(180, 20, 20, 0.2);
    border-color: rgba(220, 80, 80, 0.8);
  }

  &:disabled {
    opacity: 0.4;
    cursor: default;
  }
}
```

- [ ] **Step 4: Build and verify**

Run from `frontend/`: `ng build --configuration=development`
Expected: No TypeScript errors.

Start the dev server and navigate to `/bracket/1`:
- With DRAFT status: "Lancer les poules" button visible, clicking runs group stage and reloads (status becomes GROUP_STAGE_COMPLETE, tabs appear, groups tab shown)
- On bracket tab with GROUP_STAGE_COMPLETE: "Générer le bracket" button visible, clicking generates bracket and shows it (status becomes IN_PROGRESS, bracket tab shown)
- With IN_PROGRESS: full bracket visible, no button

- [ ] **Step 5: Commit**

```bash
git add frontend/src/app/bracket/bracket-page/bracket-page.component.ts \
        frontend/src/app/bracket/bracket-page/bracket-page.component.html \
        frontend/src/app/bracket/bracket-page/bracket-page.component.scss
git commit -m "feat: add Lancer les poules and Générer le bracket action buttons to bracket page"
```

---

## Spec Coverage Checklist

| Spec requirement | Task |
|---|---|
| `GROUP_STAGE_COMPLETE` status | Task 1 |
| `QUALIFIED` registration status | Task 1 |
| `hasGroupStage` field on Tournament entity | Task 2 |
| `status` field in TournamentBracketDataDto | Task 3 |
| BracketService reads `tournament.isHasGroupStage()` | Task 3 |
| RegistrationDAO query for QUALIFIED status | Task 4 |
| Bulk status update for qualifiers | Task 4 |
| GroupStageService pure computation | Task 5 |
| GroupStageService unit tests | Task 5 |
| TournamentService.launchGroupStage() | Task 6 |
| TournamentService.generateBracket() handles GROUP_STAGE_COMPLETE | Task 6 |
| POST /tournaments/{id}/launch-group-stage | Task 7 |
| DataInitializer: 32 players, DRAFT, no matches | Task 8 |
| bracket.models.ts status field | Task 9 |
| Service launchGroupStage() + generateBracket() | Task 9 |
| Bracket-page conditional buttons | Task 10 |
| Error: launchGroupStage status ≠ DRAFT → 400 | Task 6 |
| Error: count not divisible by 4 → 400 | Task 6 |
| Error: N/2 not power of 2 → 400 | Task 6 |
| Error: generateBracket status ≠ DRAFT/GROUP_STAGE_COMPLETE → 400 | Task 6 |
