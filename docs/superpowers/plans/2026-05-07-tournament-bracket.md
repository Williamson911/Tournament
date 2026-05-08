# Tournament Bracket System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement double-elimination bracket generation, tournament/registration endpoints, automatic player advancement on result recording, and a left-to-right Angular bracket layout with SVG connectors.

**Architecture:** Backend generates all matches upfront with `bracket_position` as the routing key (e.g. `W11`, `L21`, `GF1`). `BracketRoutingService` computes next positions algorithmically. Frontend wraps WB/LB in a vertical column and places GF + Champion in the same horizontal row using SVG connectors.

**Tech Stack:** Java 21 · Quarkus-style CDI (Weld) · Jakarta JAX-RS (Jersey) · Hibernate JPA · PostgreSQL · Lombok · JUnit Jupiter 5 · Angular 17+ (signals)

---

## File Map

**Create (backend):**
- `src/main/java/be/technifutur/tournament/services/BracketRoutingService.java`
- `src/main/java/be/technifutur/tournament/services/BracketGenerationService.java`
- `src/main/java/be/technifutur/tournament/services/MatchResultService.java`
- `src/main/java/be/technifutur/tournament/dtos/CreateTournamentDto.java`
- `src/main/java/be/technifutur/tournament/dtos/RegisterPlayerDto.java`
- `src/main/java/be/technifutur/tournament/dtos/UpdateStatusDto.java`
- `src/main/java/be/technifutur/tournament/dtos/RecordResultDto.java`
- `src/main/java/be/technifutur/tournament/resources/TournamentResource.java`
- `src/main/resources/db/V1__nullable_players.sql`
- `src/test/java/be/technifutur/tournament/services/BracketRoutingServiceTest.java`
- `src/test/java/be/technifutur/tournament/services/BracketGenerationServiceTest.java`

**Modify (backend):**
- `src/main/resources/META-INF/persistence.xml` (re-track in git)
- `src/main/java/be/technifutur/tournament/entities/Match.java` (nullable players + scores)
- `src/main/java/be/technifutur/tournament/daos/MatchDAO.java` (LEFT JOIN FETCH + findByTournamentAndBracketPosition)
- `src/main/java/be/technifutur/tournament/services/TournamentService.java` (implement create/register/generateBracket)
- `src/main/java/be/technifutur/tournament/services/MatchResultService.java` (new)
- `src/main/java/be/technifutur/tournament/resources/MatchResource.java` (add PUT /{id}/result)
- `src/main/java/be/technifutur/tournament/dtos/TournamentBracketDataDto.java` (remove bracketReset)
- `src/main/java/be/technifutur/tournament/dtos/BracketMatchDto.java` (remove isBracketReset)
- `src/main/java/be/technifutur/tournament/services/BracketService.java` (remove bracketReset logic)
- `src/main/java/be/technifutur/tournament/utils/DataInitializer.java` (add bracketPosition to existing matches)

**Create (frontend):**
- `frontend/src/app/bracket/bracket-connector/bracket-connector.component.ts`
- `frontend/src/app/bracket/bracket-connector/bracket-connector.component.scss`
- `frontend/src/app/bracket/bracket-layout/bracket-layout.component.ts`
- `frontend/src/app/bracket/bracket-layout/bracket-layout.component.html`
- `frontend/src/app/bracket/bracket-layout/bracket-layout.component.scss`

**Modify (frontend):**
- `frontend/src/app/models/bracket.models.ts` (remove bracketReset)
- `frontend/src/app/bracket/grand-final/grand-final.component.ts` (remove bracketReset input)
- `frontend/src/app/bracket/grand-final/grand-final.component.html` (remove reset block)
- `frontend/src/app/bracket/bracket-page/bracket-page.component.html` (new layout)
- `frontend/src/app/bracket/bracket-page/bracket-page.component.scss` (row direction)

---

## Task 1 — Restore persistence.xml in git + DB migration script

**Files:**
- Restore: `src/main/resources/META-INF/persistence.xml`
- Create: `src/main/resources/db/V1__nullable_players.sql`

- [ ] **Step 1: Re-track persistence.xml**

```bash
git add src/main/resources/META-INF/persistence.xml
git status
```

Expected: `persistence.xml` shows as a new file (untracked→tracked).

- [ ] **Step 2: Create the migration script directory and file**

Create `src/main/resources/db/V1__nullable_players.sql`:

```sql
-- Allow matches to exist without players assigned (for bracket slots)
ALTER TABLE match ALTER COLUMN id_player1 DROP NOT NULL;
ALTER TABLE match ALTER COLUMN id_player2 DROP NOT NULL;
ALTER TABLE match ALTER COLUMN player1_score DROP NOT NULL;
ALTER TABLE match ALTER COLUMN player2_score DROP NOT NULL;
```

- [ ] **Step 3: Run the migration against the local PostgreSQL DB**

```bash
psql -U postgres -d tournament -f src/main/resources/db/V1__nullable_players.sql
```

Expected output:
```
ALTER TABLE
ALTER TABLE
ALTER TABLE
ALTER TABLE
```

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/META-INF/persistence.xml src/main/resources/db/V1__nullable_players.sql
git commit -m "fix: restore persistence.xml tracking + DB migration for nullable match players"
```

---

## Task 2 — Match entity: nullable player1, player2, scores

**Files:**
- Modify: `src/main/java/be/technifutur/tournament/entities/Match.java`

- [ ] **Step 1: Remove `nullable = false` from player1, player2, player1Score, player2Score**

In `Match.java`, change the four annotations:

```java
// player1 — was: optional = false
@Getter @Setter
@ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
@JoinColumn(name = "id_player1")
private Player player1;

// player2 — was: optional = false
@Getter @Setter
@ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
@JoinColumn(name = "id_player2")
private Player player2;

// player1Score — was: nullable = false
@Getter @Setter
@Column(name = "player1_score")
private Integer player1Score;

// player2Score — was: nullable = false
@Getter @Setter
@Column(name = "player2_score")
private Integer player2Score;
```

- [ ] **Step 2: Update DataInitializer to set bracketPosition on existing matches**

In `DataInitializer.java`, find the match builder calls and add `.bracketPosition(...)`:

```java
// Match kevin vs laura — WB R1 M1
Match match1 = Match.builder()
    .tournament(tournament).player1(kevin).player2(laura)
    .numberRounds(3).status(MatchStatus.FINISHED)
    .bracketStage(BracketStage.WINNERS_BRACKET).roundNumber(1)
    .bracketPosition("W11")           // ← add this
    .player1Score(2).player2Score(1)
    .scheduledAt(now).startedAt(now).finishedAt(now)
    .build();

// Match yassine vs sofia — WB R1 M2
Match match2 = Match.builder()
    .tournament(tournament).player1(yassine).player2(sofia)
    .numberRounds(3).status(MatchStatus.FINISHED)
    .bracketStage(BracketStage.WINNERS_BRACKET).roundNumber(1)
    .bracketPosition("W12")           // ← add this
    .player1Score(2).player2Score(0)
    .scheduledAt(now).startedAt(now).finishedAt(now)
    .build();

// Grand Final kevin vs yassine
Match grandFinal = Match.builder()
    .tournament(tournament).player1(kevin).player2(yassine)
    .numberRounds(3).status(MatchStatus.FINISHED)
    .bracketStage(BracketStage.GRAND_FINAL).roundNumber(1)
    .bracketPosition("GF1")           // ← add this
    .player1Score(3).player2Score(2)
    .scheduledAt(now).startedAt(now).finishedAt(now)
    .build();
```

- [ ] **Step 3: Verify app starts without error**

Start the server and check no Hibernate validation error about nullable columns.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/be/technifutur/tournament/entities/Match.java
git add src/main/java/be/technifutur/tournament/utils/DataInitializer.java
git commit -m "feat: make match player/score fields nullable for bracket slot pre-creation"
```

---

## Task 3 — MatchDAO: fix JOIN FETCH + add findByTournamentAndBracketPosition

**Files:**
- Modify: `src/main/java/be/technifutur/tournament/daos/MatchDAO.java`

- [ ] **Step 1: Fix findById to use LEFT JOIN FETCH**

Replace the existing `findById` body:

```java
@Override
public Optional<Match> findById(Integer id) {
    try (var em = emfProvider.get().createEntityManager()) {
        return em.createQuery(
            "SELECT m FROM Match m JOIN FETCH m.tournament " +
            "LEFT JOIN FETCH m.player1 p1 LEFT JOIN FETCH p1.fighterMain " +
            "LEFT JOIN FETCH m.player2 p2 LEFT JOIN FETCH p2.fighterMain " +
            "WHERE m.id = :id", Match.class)
            .setParameter("id", id)
            .getResultStream().findFirst();
    }
}
```

- [ ] **Step 2: Fix findByTournamentWithPlayers to use LEFT JOIN FETCH**

Replace the existing `findByTournamentWithPlayers` body:

```java
public List<Match> findByTournamentWithPlayers(int tournamentId) {
    try (var em = emfProvider.get().createEntityManager()) {
        return em.createQuery(
            "SELECT m FROM Match m " +
            "LEFT JOIN FETCH m.player1 p1 LEFT JOIN FETCH p1.fighterMain " +
            "LEFT JOIN FETCH m.player2 p2 LEFT JOIN FETCH p2.fighterMain " +
            "WHERE m.tournament.id = :tid " +
            "ORDER BY m.roundNumber ASC NULLS LAST, m.id ASC",
            Match.class)
            .setParameter("tid", tournamentId)
            .getResultList();
    }
}
```

- [ ] **Step 3: Fix findAllWithRelations to use LEFT JOIN FETCH**

```java
public List<Match> findAllWithRelations() {
    try (var em = emfProvider.get().createEntityManager()) {
        return em.createQuery(
            "SELECT m FROM Match m JOIN FETCH m.tournament " +
            "LEFT JOIN FETCH m.player1 LEFT JOIN FETCH m.player2",
            Match.class).getResultList();
    }
}
```

- [ ] **Step 4: Add findByTournamentAndBracketPosition**

Add after `findByTournamentOrdered`:

```java
public Optional<Match> findByTournamentAndBracketPosition(int tournamentId, String bracketPosition) {
    try (var em = emfProvider.get().createEntityManager()) {
        return em.createQuery(
            "SELECT m FROM Match m " +
            "LEFT JOIN FETCH m.player1 LEFT JOIN FETCH m.player2 " +
            "WHERE m.tournament.id = :tid AND m.bracketPosition = :pos",
            Match.class)
            .setParameter("tid", tournamentId)
            .setParameter("pos", bracketPosition)
            .getResultStream().findFirst();
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/be/technifutur/tournament/daos/MatchDAO.java
git commit -m "fix: LEFT JOIN FETCH for nullable players + add findByTournamentAndBracketPosition"
```

---

## Task 4 — BracketRoutingService + unit tests

**Files:**
- Create: `src/main/java/be/technifutur/tournament/services/BracketRoutingService.java`
- Create: `src/test/java/be/technifutur/tournament/services/BracketRoutingServiceTest.java`

- [ ] **Step 1: Write the failing tests first**

Create `src/test/java/be/technifutur/tournament/services/BracketRoutingServiceTest.java`:

```java
package be.technifutur.tournament.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BracketRoutingServiceTest {

    private BracketRoutingService service;

    @BeforeEach
    void setUp() { service = new BracketRoutingService(); }

    // ── 8-player WB routing ──────────────────────────────────────
    @Test void wbR1_M1_winner_goesToW21() {
        var r = service.compute("W11", 8);
        assertEquals("W21", r.nextWinnerPosition());
    }
    @Test void wbR1_M2_winner_goesToW21() {
        var r = service.compute("W12", 8);
        assertEquals("W21", r.nextWinnerPosition());
    }
    @Test void wbR1_M3_winner_goesToW22() {
        var r = service.compute("W13", 8);
        assertEquals("W22", r.nextWinnerPosition());
    }
    @Test void wbR1_M1_loser_goesToL11() {
        var r = service.compute("W11", 8);
        assertEquals("L11", r.nextLoserPosition());
    }
    @Test void wbR1_M2_loser_goesToL11() {
        var r = service.compute("W12", 8);
        assertEquals("L11", r.nextLoserPosition());
    }
    @Test void wbR1_M3_loser_goesToL12() {
        var r = service.compute("W13", 8);
        assertEquals("L12", r.nextLoserPosition());
    }
    @Test void wbR2_M1_winner_goesToW31() {
        var r = service.compute("W21", 8);
        assertEquals("W31", r.nextWinnerPosition());
    }
    @Test void wbR2_M1_loser_goesToL21() {
        var r = service.compute("W21", 8);
        assertEquals("L21", r.nextLoserPosition());
    }
    @Test void wbR2_M2_loser_goesToL22() {
        var r = service.compute("W22", 8);
        assertEquals("L22", r.nextLoserPosition());
    }
    @Test void wbFinal_winner_goesToGF1() {
        var r = service.compute("W31", 8);
        assertEquals("GF1", r.nextWinnerPosition());
    }
    @Test void wbFinal_loser_goesToL41() {
        var r = service.compute("W31", 8);
        assertEquals("L41", r.nextLoserPosition());
    }

    // ── 8-player LB routing ──────────────────────────────────────
    @Test void lbR1_M1_winner_goesToL21() {
        var r = service.compute("L11", 8);
        assertEquals("L21", r.nextWinnerPosition());
    }
    @Test void lbR1_M1_loser_isEliminated() {
        var r = service.compute("L11", 8);
        assertNull(r.nextLoserPosition());
    }
    @Test void lbR1_M2_winner_goesToL22() {
        var r = service.compute("L12", 8);
        assertEquals("L22", r.nextWinnerPosition());
    }
    @Test void lbR2_M1_winner_goesToL31() {
        var r = service.compute("L21", 8);
        assertEquals("L31", r.nextWinnerPosition());
    }
    @Test void lbR2_M2_winner_goesToL31() {
        var r = service.compute("L22", 8);
        assertEquals("L31", r.nextWinnerPosition());
    }
    @Test void lbR3_M1_winner_goesToL41() {
        var r = service.compute("L31", 8);
        assertEquals("L41", r.nextWinnerPosition());
    }
    @Test void lbFinal_winner_goesToGF1() {
        var r = service.compute("L41", 8);
        assertEquals("GF1", r.nextWinnerPosition());
    }
    @Test void lbFinal_loser_isEliminated() {
        var r = service.compute("L41", 8);
        assertNull(r.nextLoserPosition());
    }

    // ── GF routing ───────────────────────────────────────────────
    @Test void gf_winner_hasNoNextPosition() {
        var r = service.compute("GF1", 8);
        assertNull(r.nextWinnerPosition());
    }
    @Test void gf_loser_isEliminated() {
        var r = service.compute("GF1", 8);
        assertNull(r.nextLoserPosition());
    }
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```bash
mvn test -pl . -Dtest=BracketRoutingServiceTest
```

Expected: compilation error — `BracketRoutingService` does not exist yet.

- [ ] **Step 3: Implement BracketRoutingService**

Create `src/main/java/be/technifutur/tournament/services/BracketRoutingService.java`:

```java
package be.technifutur.tournament.services;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BracketRoutingService {

    public record RoutingResult(String nextWinnerPosition, String nextLoserPosition) {}

    /**
     * Computes next bracket positions from a given match position.
     * totalPlayers must be a power of 2 (4, 8, 16...).
     */
    public RoutingResult compute(String bracketPosition, int totalPlayers) {
        int wbRounds = (int)(Math.log(totalPlayers) / Math.log(2));
        int lbRounds = 2 * (wbRounds - 1);

        if (bracketPosition.startsWith("GF")) {
            return new RoutingResult(null, null);
        }

        if (bracketPosition.startsWith("W")) {
            int round = Character.getNumericValue(bracketPosition.charAt(1));
            int match = Character.getNumericValue(bracketPosition.charAt(2));

            if (round == wbRounds) {
                return new RoutingResult("GF1", "L" + lbRounds + "1");
            }

            String nextWinner = "W" + (round + 1) + (int)Math.ceil(match / 2.0);
            String nextLoser = (round == 1)
                ? "L1" + (int)Math.ceil(match / 2.0)
                : "L" + (round * 2 - 2) + match;
            return new RoutingResult(nextWinner, nextLoser);
        }

        if (bracketPosition.startsWith("L")) {
            int round = Character.getNumericValue(bracketPosition.charAt(1));
            int match = Character.getNumericValue(bracketPosition.charAt(2));

            if (round == lbRounds) {
                return new RoutingResult("GF1", null);
            }

            String nextWinner = (round % 2 == 1)
                ? "L" + (round + 1) + match
                : "L" + (round + 1) + (int)Math.ceil(match / 2.0);
            return new RoutingResult(nextWinner, null);
        }

        throw new IllegalArgumentException("Unknown bracket position: " + bracketPosition);
    }
}
```

- [ ] **Step 4: Run tests — all must pass**

```bash
mvn test -pl . -Dtest=BracketRoutingServiceTest
```

Expected: `Tests run: 22, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/be/technifutur/tournament/services/BracketRoutingService.java
git add src/test/java/be/technifutur/tournament/services/BracketRoutingServiceTest.java
git commit -m "feat: BracketRoutingService — double elimination position routing"
```

---

## Task 5 — BracketGenerationService + unit tests

**Files:**
- Create: `src/main/java/be/technifutur/tournament/services/BracketGenerationService.java`
- Create: `src/test/java/be/technifutur/tournament/services/BracketGenerationServiceTest.java`

- [ ] **Step 1: Write the failing tests**

Create `src/test/java/be/technifutur/tournament/services/BracketGenerationServiceTest.java`:

```java
package be.technifutur.tournament.services;

import be.technifutur.tournament.entities.Match;
import be.technifutur.tournament.entities.Player;
import be.technifutur.tournament.entities.Tournament;
import be.technifutur.tournament.enums.BracketStage;
import be.technifutur.tournament.enums.MatchStatus;
import be.technifutur.tournament.enums.TournamentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

class BracketGenerationServiceTest {

    private BracketGenerationService service;
    private Tournament tournament;

    @BeforeEach
    void setUp() {
        service = new BracketGenerationService();
        tournament = Tournament.builder().name("Test").status(TournamentStatus.DRAFT).build();
    }

    private List<Player> players(int count) {
        return IntStream.rangeClosed(1, count)
            .mapToObj(i -> Player.builder().username("p" + i).build())
            .toList();
    }

    @Test void eightPlayers_generates14Matches() {
        var matches = service.generate(tournament, players(8));
        assertEquals(14, matches.size());
    }
    @Test void fourPlayers_generates6Matches() {
        var matches = service.generate(tournament, players(4));
        assertEquals(6, matches.size());
    }
    @Test void eightPlayers_hasCorrectWBMatchCount() {
        var matches = service.generate(tournament, players(8));
        long wb = matches.stream().filter(m -> m.getBracketStage() == BracketStage.WINNERS_BRACKET).count();
        assertEquals(7, wb); // 4+2+1
    }
    @Test void eightPlayers_hasCorrectLBMatchCount() {
        var matches = service.generate(tournament, players(8));
        long lb = matches.stream().filter(m -> m.getBracketStage() == BracketStage.LOSERS_BRACKET).count();
        assertEquals(6, lb); // 2+2+1+1
    }
    @Test void eightPlayers_hasOneGF() {
        var matches = service.generate(tournament, players(8));
        long gf = matches.stream().filter(m -> m.getBracketStage() == BracketStage.GRAND_FINAL).count();
        assertEquals(1, gf);
    }
    @Test void wbR1Matches_havePlayersAssigned() {
        var matches = service.generate(tournament, players(8));
        matches.stream()
            .filter(m -> m.getBracketPosition().startsWith("W1"))
            .forEach(m -> {
                assertNotNull(m.getPlayer1());
                assertNotNull(m.getPlayer2());
            });
    }
    @Test void futureMatches_haveNullPlayers() {
        var matches = service.generate(tournament, players(8));
        matches.stream()
            .filter(m -> !m.getBracketPosition().startsWith("W1"))
            .forEach(m -> assertTrue(m.getPlayer1() == null || m.getPlayer2() == null,
                "Future match " + m.getBracketPosition() + " should have at least one null player"));
    }
    @Test void allMatchesAreScheduled() {
        var matches = service.generate(tournament, players(8));
        matches.forEach(m -> assertEquals(MatchStatus.SCHEDULED, m.getStatus()));
    }
    @Test void bracketPositionsAreUnique() {
        var matches = service.generate(tournament, players(8));
        long distinct = matches.stream().map(Match::getBracketPosition).distinct().count();
        assertEquals(14, distinct);
    }
    @Test void oddPlayerCount_throwsException() {
        assertThrows(Exception.class, () -> service.generate(tournament, players(6)));
    }
    @Test void gf1_hasCorrectPosition() {
        var matches = service.generate(tournament, players(8));
        assertTrue(matches.stream().anyMatch(m -> "GF1".equals(m.getBracketPosition())));
    }
}
```

- [ ] **Step 2: Run tests — expect compilation error**

```bash
mvn test -pl . -Dtest=BracketGenerationServiceTest
```

- [ ] **Step 3: Implement BracketGenerationService**

Create `src/main/java/be/technifutur/tournament/services/BracketGenerationService.java`:

```java
package be.technifutur.tournament.services;

import be.technifutur.tournament.entities.Match;
import be.technifutur.tournament.entities.Player;
import be.technifutur.tournament.entities.Tournament;
import be.technifutur.tournament.enums.BracketStage;
import be.technifutur.tournament.enums.MatchStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class BracketGenerationService {

    public List<Match> generate(Tournament tournament, List<Player> players) {
        int n = players.size();
        if (n < 4 || (n & (n - 1)) != 0)
            throw new BadRequestException("Player count must be a power of 2 (4, 8, 16...)");

        int wbRounds = (int)(Math.log(n) / Math.log(2));
        int lbRounds = 2 * (wbRounds - 1);

        List<Player> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled);

        List<Match> matches = new ArrayList<>();

        // Winners Bracket
        for (int r = 1; r <= wbRounds; r++) {
            int matchCount = n / (int)Math.pow(2, r);
            for (int m = 1; m <= matchCount; m++) {
                Player p1 = null, p2 = null;
                if (r == 1) {
                    p1 = shuffled.get((m - 1) * 2);
                    p2 = shuffled.get((m - 1) * 2 + 1);
                }
                matches.add(buildMatch(tournament, "W" + r + m, p1, p2, BracketStage.WINNERS_BRACKET, r));
            }
        }

        // Losers Bracket
        for (int r = 1; r <= lbRounds; r++) {
            int matchCount = n / (int)Math.pow(2, (int)Math.ceil(r / 2.0) + 1);
            for (int m = 1; m <= matchCount; m++) {
                matches.add(buildMatch(tournament, "L" + r + m, null, null, BracketStage.LOSERS_BRACKET, r));
            }
        }

        // Grand Final
        matches.add(buildMatch(tournament, "GF1", null, null, BracketStage.GRAND_FINAL, 1));

        return matches;
    }

    private Match buildMatch(Tournament tournament, String pos, Player p1, Player p2,
                              BracketStage stage, int round) {
        return Match.builder()
            .tournament(tournament)
            .bracketPosition(pos)
            .bracketStage(stage)
            .roundNumber(round)
            .player1(p1).player2(p2)
            .player1Score(null).player2Score(null)
            .numberRounds(3)
            .status(MatchStatus.SCHEDULED)
            .build();
    }
}
```

- [ ] **Step 4: Run tests — all must pass**

```bash
mvn test -pl . -Dtest=BracketGenerationServiceTest
```

Expected: `Tests run: 11, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/be/technifutur/tournament/services/BracketGenerationService.java
git add src/test/java/be/technifutur/tournament/services/BracketGenerationServiceTest.java
git commit -m "feat: BracketGenerationService — full double-elimination bracket structure"
```

---

## Task 6 — TournamentService: create, register, generateBracket

**Files:**
- Modify: `src/main/java/be/technifutur/tournament/services/TournamentService.java`
- Create: `src/main/java/be/technifutur/tournament/dtos/CreateTournamentDto.java`
- Create: `src/main/java/be/technifutur/tournament/dtos/RegisterPlayerDto.java`
- Create: `src/main/java/be/technifutur/tournament/dtos/UpdateStatusDto.java`

- [ ] **Step 1: Create the DTOs**

`src/main/java/be/technifutur/tournament/dtos/CreateTournamentDto.java`:
```java
package be.technifutur.tournament.dtos;
import java.time.LocalDateTime;
public record CreateTournamentDto(String name, LocalDateTime startDate) {}
```

`src/main/java/be/technifutur/tournament/dtos/RegisterPlayerDto.java`:
```java
package be.technifutur.tournament.dtos;
public record RegisterPlayerDto(int playerId) {}
```

`src/main/java/be/technifutur/tournament/dtos/UpdateStatusDto.java`:
```java
package be.technifutur.tournament.dtos;
import be.technifutur.tournament.enums.TournamentStatus;
public record UpdateStatusDto(TournamentStatus status) {}
```

- [ ] **Step 2: Implement TournamentService**

Replace the empty body of `TournamentService.java`:

```java
package be.technifutur.tournament.services;

import be.technifutur.tournament.daos.MatchDAO;
import be.technifutur.tournament.daos.PlayerDAO;
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

@ApplicationScoped
public class TournamentService {

    @Inject TournamentDAO tournamentDAO;
    @Inject RegistrationDAO registrationDAO;
    @Inject PlayerDAO playerDAO;
    @Inject MatchDAO matchDAO;
    @Inject BracketGenerationService generationService;
    @Inject BracketService bracketService;

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

    public TournamentBracketDataDto generateBracket(int tournamentId) {
        Tournament t = tournamentDAO.findById(tournamentId)
            .orElseThrow(() -> new NotFoundException("Tournament not found"));
        List<Registration> registrations = registrationDAO.findByTournamentWithStatus(tournamentId);
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

- [ ] **Step 3: Verify `RegistrationDAO.findByPlayerAndTournament` exists**

Check that `RegistrationDAO` has a `findByPlayerAndTournament(int playerId, int tournamentId)` returning `Optional<Registration>`. If the method is named differently, align the call above.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/be/technifutur/tournament/services/TournamentService.java
git add src/main/java/be/technifutur/tournament/dtos/CreateTournamentDto.java
git add src/main/java/be/technifutur/tournament/dtos/RegisterPlayerDto.java
git add src/main/java/be/technifutur/tournament/dtos/UpdateStatusDto.java
git commit -m "feat: TournamentService — create, register, generateBracket, updateStatus"
```

---

## Task 7 — TournamentResource: REST endpoints

**Files:**
- Create: `src/main/java/be/technifutur/tournament/resources/TournamentResource.java`

- [ ] **Step 1: Create TournamentResource**

```java
package be.technifutur.tournament.resources;

import be.technifutur.tournament.dtos.*;
import be.technifutur.tournament.services.TournamentService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/tournaments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TournamentResource {

    @Inject TournamentService tournamentService;

    @POST
    public Response create(CreateTournamentDto dto) {
        var tournament = tournamentService.create(dto.name(), dto.startDate());
        return Response.status(Response.Status.CREATED).entity(tournament).build();
    }

    @POST
    @Path("/{id}/registrations")
    public Response register(@PathParam("id") int id, RegisterPlayerDto dto) {
        var registration = tournamentService.register(id, dto.playerId());
        return Response.status(Response.Status.CREATED).entity(registration).build();
    }

    @DELETE
    @Path("/{id}/registrations/{playerId}")
    public Response unregister(@PathParam("id") int id, @PathParam("playerId") int playerId) {
        tournamentService.unregister(id, playerId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/generate-bracket")
    public Response generateBracket(@PathParam("id") int id) {
        var data = tournamentService.generateBracket(id);
        return Response.ok(data).build();
    }

    @PUT
    @Path("/{id}/status")
    public Response updateStatus(@PathParam("id") int id, UpdateStatusDto dto) {
        var tournament = tournamentService.updateStatus(id, dto.status());
        return Response.ok(tournament).build();
    }
}
```

- [ ] **Step 2: Register TournamentResource in the application**

Check `HelloApplication.java` (or equivalent `Application` subclass). If it uses a manual `getSingletons()` or `getClasses()` set, add `TournamentResource.class` to it. If it uses `@ApplicationPath` with auto-scan, no change needed.

- [ ] **Step 3: Test manually**

Start the server, then:

```bash
# Create a tournament
curl -s -X POST http://localhost:8080/api/tournaments \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Tournament","startDate":"2026-06-01T10:00:00"}' | jq .

# Register 8 players (adjust playerIds to match your DB)
for id in 1 2 3 4 5 6 7 8; do
  curl -s -X POST http://localhost:8080/api/tournaments/2/registrations \
    -H "Content-Type: application/json" \
    -d "{\"playerId\":$id}"
done

# Generate bracket
curl -s -X POST http://localhost:8080/api/tournaments/2/generate-bracket | jq .
```

Expected: 201 on create, then a full bracket JSON with 14 matches.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/be/technifutur/tournament/resources/TournamentResource.java
git commit -m "feat: TournamentResource — POST /tournaments, registrations, generate-bracket, status"
```

---

## Task 8 — MatchResultService: record result + auto-advance

**Files:**
- Create: `src/main/java/be/technifutur/tournament/services/MatchResultService.java`
- Create: `src/main/java/be/technifutur/tournament/dtos/RecordResultDto.java`

- [ ] **Step 1: Create RecordResultDto**

`src/main/java/be/technifutur/tournament/dtos/RecordResultDto.java`:
```java
package be.technifutur.tournament.dtos;
import be.technifutur.tournament.enums.FinishType;
public record RecordResultDto(int player1Score, int player2Score, FinishType finishType) {}
```

- [ ] **Step 2: Implement MatchResultService**

Create `src/main/java/be/technifutur/tournament/services/MatchResultService.java`:

```java
package be.technifutur.tournament.services;

import be.technifutur.tournament.daos.MatchDAO;
import be.technifutur.tournament.daos.RegistrationDAO;
import be.technifutur.tournament.dtos.TournamentBracketDataDto;
import be.technifutur.tournament.entities.Match;
import be.technifutur.tournament.entities.Player;
import be.technifutur.tournament.enums.FinishType;
import be.technifutur.tournament.enums.MatchStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDateTime;

@ApplicationScoped
public class MatchResultService {

    @Inject MatchDAO matchDAO;
    @Inject RegistrationDAO registrationDAO;
    @Inject BracketRoutingService routingService;
    @Inject BracketService bracketService;

    public TournamentBracketDataDto recordResult(int matchId, int p1Score, int p2Score, FinishType finishType) {
        Match match = matchDAO.findById(matchId)
            .orElseThrow(() -> new NotFoundException("Match not found"));

        if (match.getStatus() == MatchStatus.FINISHED)
            throw new WebApplicationException("Match already finished", 409);
        if (match.getPlayer1() == null || match.getPlayer2() == null)
            throw new BadRequestException("Match is not ready — players not yet assigned");
        if (p1Score == p2Score)
            throw new BadRequestException("Tie scores are not allowed");

        match.setPlayer1Score(p1Score);
        match.setPlayer2Score(p2Score);
        match.setFinishType(finishType);
        match.setStatus(MatchStatus.FINISHED);
        match.setFinishedAt(LocalDateTime.now());
        matchDAO.update(match);

        Player winner = p1Score > p2Score ? match.getPlayer1() : match.getPlayer2();
        Player loser  = p1Score > p2Score ? match.getPlayer2() : match.getPlayer1();

        int tournamentId = match.getTournament().getId();
        int totalPlayers = registrationDAO.findByTournamentWithStatus(tournamentId).size();

        var routing = routingService.compute(match.getBracketPosition(), totalPlayers);

        if (routing.nextWinnerPosition() != null)
            placePlayer(tournamentId, routing.nextWinnerPosition(), winner);
        if (routing.nextLoserPosition() != null)
            placePlayer(tournamentId, routing.nextLoserPosition(), loser);

        return bracketService.buildBracketData(tournamentId);
    }

    private void placePlayer(int tournamentId, String position, Player player) {
        Match target = matchDAO.findByTournamentAndBracketPosition(tournamentId, position)
            .orElseThrow(() -> new NotFoundException("Target match at " + position + " not found"));
        if (target.getPlayer1() == null) {
            target.setPlayer1(player);
        } else {
            target.setPlayer2(player);
        }
        matchDAO.update(target);
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/be/technifutur/tournament/services/MatchResultService.java
git add src/main/java/be/technifutur/tournament/dtos/RecordResultDto.java
git commit -m "feat: MatchResultService — record result with automatic double-elim advancement"
```

---

## Task 9 — MatchResource: add PUT /{id}/result

**Files:**
- Modify: `src/main/java/be/technifutur/tournament/resources/MatchResource.java`

- [ ] **Step 1: Inject MatchResultService and add endpoint**

In `MatchResource.java`, add the injection and new method:

```java
@Inject
private MatchResultService matchResultService;

@PUT
@Path("/{id}/result")
public Response recordResult(@PathParam("id") int id, RecordResultDto dto) {
    var bracketData = matchResultService.recordResult(id, dto.player1Score(), dto.player2Score(), dto.finishType());
    return Response.ok(bracketData).build();
}
```

Add the import: `import be.technifutur.tournament.dtos.RecordResultDto;`

- [ ] **Step 2: Test manually**

```bash
# Record result for match 1 of a generated bracket (adjust matchId)
curl -s -X PUT http://localhost:8080/api/matches/5/result \
  -H "Content-Type: application/json" \
  -d '{"player1Score":2,"player2Score":1,"finishType":"KO"}' | jq '.winnersBracket'
```

Expected: the winning player now appears in `W21` slot of the returned bracket.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/be/technifutur/tournament/resources/MatchResource.java
git commit -m "feat: PUT /matches/{id}/result — record score and advance players"
```

---

## Task 10 — Remove bracketReset from DTOs and BracketService

**Files:**
- Modify: `src/main/java/be/technifutur/tournament/dtos/TournamentBracketDataDto.java`
- Modify: `src/main/java/be/technifutur/tournament/dtos/BracketMatchDto.java`
- Modify: `src/main/java/be/technifutur/tournament/services/BracketService.java`

- [ ] **Step 1: Remove bracketReset from TournamentBracketDataDto**

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
    MatchParticipantDto champion
) {}
```

- [ ] **Step 2: Remove isBracketReset from BracketMatchDto**

```java
package be.technifutur.tournament.dtos;
import com.fasterxml.jackson.annotation.JsonProperty;

public record BracketMatchDto(
    int matchId,
    MatchParticipantDto participant1,
    MatchParticipantDto participant2,
    @JsonProperty("isComplete") boolean isComplete
) {}
```

- [ ] **Step 3: Update BracketService.toMatchDto to remove isBracketReset**

In `BracketService.java`, find `toMatchDto` and update the return:

```java
private BracketMatchDto toMatchDto(Match m) {
    boolean complete = m.getStatus() == MatchStatus.FINISHED;
    BracketStage stage = m.getBracketStage();
    boolean p1Wins = m.getPlayer1Score() != null && m.getPlayer2Score() != null
        && m.getPlayer1Score() > m.getPlayer2Score();

    MatchParticipantDto p1 = m.getPlayer1() != null
        ? toParticipantDto(m.getPlayer1(), m.getPlayer1Score() != null ? m.getPlayer1Score() : 0,
            complete && p1Wins, complete && isEliminated(stage, !p1Wins))
        : null;
    MatchParticipantDto p2 = m.getPlayer2() != null
        ? toParticipantDto(m.getPlayer2(), m.getPlayer2Score() != null ? m.getPlayer2Score() : 0,
            complete && !p1Wins, complete && isEliminated(stage, p1Wins))
        : null;

    return new BracketMatchDto(m.getId(), p1, p2, complete);
}
```

- [ ] **Step 4: Update buildBracketData to remove bracketReset from constructor call**

In `BracketService.buildBracketData`, change the return statement:

```java
return new TournamentBracketDataDto(
    tournament.getId(), tournament.getName(), hasGroupStage,
    groups, winnersBracket, losersBracket,
    grandFinal, champion
);
```

- [ ] **Step 5: Compile and verify**

```bash
mvn compile
```

Expected: BUILD SUCCESS with no errors.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/be/technifutur/tournament/dtos/TournamentBracketDataDto.java
git add src/main/java/be/technifutur/tournament/dtos/BracketMatchDto.java
git add src/main/java/be/technifutur/tournament/services/BracketService.java
git commit -m "refactor: remove bracketReset — GF loser is simply eliminated"
```

---

## Task 11 — Frontend: update models and remove bracketReset

**Files:**
- Modify: `frontend/src/app/models/bracket.models.ts`
- Modify: `frontend/src/app/bracket/grand-final/grand-final.component.ts`
- Modify: `frontend/src/app/bracket/grand-final/grand-final.component.html`

- [ ] **Step 1: Update bracket.models.ts**

Remove `bracketReset` from `TournamentBracketData` and `isBracketReset` from `BracketMatch`:

```typescript
export interface BracketMatch {
  matchId: number;
  participant1: MatchParticipant | null;
  participant2: MatchParticipant | null;
  isComplete: boolean;
}

export interface TournamentBracketData {
  tournamentId: number;
  tournamentName: string;
  hasGroupStage: boolean;
  groups: TournamentGroup[];
  winnersBracket: BracketRound[];
  losersBracket: BracketRound[];
  grandFinal: BracketMatch | null;
  champion: MatchParticipant | null;
}
```

- [ ] **Step 2: Update GrandFinalComponent — remove bracketReset input**

`grand-final.component.ts`:
```typescript
import { Component, input } from '@angular/core';
import { BracketMatchComponent } from '../bracket-match/bracket-match.component';
import { BracketMatch } from '../../models/bracket.models';

@Component({
  selector: 'app-grand-final',
  imports: [BracketMatchComponent],
  templateUrl: './grand-final.component.html',
  styleUrl: './grand-final.component.scss'
})
export class GrandFinalComponent {
  grandFinal = input.required<BracketMatch>();
}
```

`grand-final.component.html`:
```html
<div class="gf-section">
  <div class="gf-label">GRAND FINAL</div>
  <app-bracket-match [match]="grandFinal()" />
</div>
```

- [ ] **Step 3: Update bracket-slot to handle null participant**

In `bracket-slot.component.html`, the template already handles `participant()` being null via `??` operators. Verify `participant()?.isWinner` and `participant()?.isEliminated` work with null — they do since `?.` is used. No change needed.

- [ ] **Step 4: Run Angular build to check for type errors**

```bash
cd frontend && npx ng build --configuration development 2>&1 | tail -20
```

Expected: no type errors.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/app/models/bracket.models.ts
git add frontend/src/app/bracket/grand-final/grand-final.component.ts
git add frontend/src/app/bracket/grand-final/grand-final.component.html
git commit -m "refactor(frontend): remove bracketReset from models and GrandFinalComponent"
```

---

## Task 12 — Frontend: bracket-connector component (SVG)

**Files:**
- Create: `frontend/src/app/bracket/bracket-connector/bracket-connector.component.ts`
- Create: `frontend/src/app/bracket/bracket-connector/bracket-connector.component.scss`

- [ ] **Step 1: Create the component**

`bracket-connector.component.ts`:

```typescript
import { Component, computed, input } from '@angular/core';
import { NgFor } from '@angular/common';

interface Pair { topY: number; botY: number; mid: number; }

@Component({
  selector: 'app-bracket-connector',
  imports: [],
  template: `
    <svg [attr.width]="WIDTH" [attr.height]="svgHeight()"
         [attr.viewBox]="'0 0 ' + WIDTH + ' ' + svgHeight()" fill="none">
      @for (pair of pairs(); track $index) {
        <path [attr.d]="'M2,' + pair.topY + ' H26 V' + pair.mid + ' H' + WIDTH"
              stroke="rgba(180,0,0,.42)" stroke-width="1.5"/>
        <path [attr.d]="'M2,' + pair.botY + ' H26 V' + pair.mid"
              stroke="rgba(180,0,0,.42)" stroke-width="1.5"/>
        <circle [attr.cx]="WIDTH" [attr.cy]="pair.mid" r="3.5" fill="rgba(230,0,0,.75)"/>
        <circle [attr.cx]="2" [attr.cy]="pair.topY" r="2.5" fill="rgba(180,0,0,.55)"/>
        <circle [attr.cx]="2" [attr.cy]="pair.botY" r="2.5" fill="rgba(180,0,0,.55)"/>
      }
    </svg>
  `,
  styleUrl: './bracket-connector.component.scss'
})
export class BracketConnectorComponent {
  /** Number of matches in the preceding round (must be even). */
  matchCount = input.required<number>();

  protected readonly WIDTH = 52;
  private readonly SLOT_H = 62;
  private readonly VS_H = 14;
  private readonly MATCH_H = this.SLOT_H * 2 + this.VS_H;  // 138
  private readonly GAP = 24;
  private readonly PAIR_H = this.MATCH_H * 2 + this.GAP;   // 300

  protected pairs = computed<Pair[]>(() => {
    const pairCount = Math.ceil(this.matchCount() / 2);
    return Array.from({ length: pairCount }, (_, i) => {
      const offset = i * (this.PAIR_H + this.GAP);
      const topY = offset + this.MATCH_H / 2;
      const botY = offset + this.MATCH_H + this.GAP + this.MATCH_H / 2;
      return { topY, botY, mid: (topY + botY) / 2 };
    });
  });

  protected svgHeight = computed(() => {
    const p = this.pairs();
    if (!p.length) return 0;
    return p[p.length - 1].botY + this.MATCH_H / 2;
  });
}
```

`bracket-connector.component.scss`:
```scss
:host { display: block; align-self: stretch; }
svg { display: block; height: 100%; }
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/app/bracket/bracket-connector/
git commit -m "feat(frontend): BracketConnectorComponent — dynamic SVG bracket connectors"
```

---

## Task 13 — Frontend: bracket-layout component

**Files:**
- Create: `frontend/src/app/bracket/bracket-layout/bracket-layout.component.ts`
- Create: `frontend/src/app/bracket/bracket-layout/bracket-layout.component.html`
- Create: `frontend/src/app/bracket/bracket-layout/bracket-layout.component.scss`

- [ ] **Step 1: Create the component**

`bracket-layout.component.ts`:
```typescript
import { Component, input } from '@angular/core';
import { WinnersBracketComponent } from '../winners-bracket/winners-bracket.component';
import { LosersBracketComponent } from '../losers-bracket/losers-bracket.component';
import { BracketRound } from '../../models/bracket.models';

@Component({
  selector: 'app-bracket-layout',
  imports: [WinnersBracketComponent, LosersBracketComponent],
  templateUrl: './bracket-layout.component.html',
  styleUrl: './bracket-layout.component.scss'
})
export class BracketLayoutComponent {
  winnersBracket = input.required<BracketRound[]>();
  losersBracket = input.required<BracketRound[]>();
}
```

`bracket-layout.component.html`:
```html
<div class="layout">
  <app-winners-bracket [rounds]="winnersBracket()" />
  @if (losersBracket().length > 0) {
    <div class="lb-separator"></div>
    <app-losers-bracket [rounds]="losersBracket()" />
  }
</div>
```

`bracket-layout.component.scss`:
```scss
.layout {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.lb-separator {
  height: 40px;
  width: 100%;
  border-top: 1px solid rgba(120, 0, 0, 0.25);
  margin: 8px 0;
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/app/bracket/bracket-layout/
git commit -m "feat(frontend): BracketLayoutComponent — stacks WB and LB vertically"
```

---

## Task 14 — Frontend: bracket-page left-to-right layout

**Files:**
- Modify: `frontend/src/app/bracket/bracket-page/bracket-page.component.html`
- Modify: `frontend/src/app/bracket/bracket-page/bracket-page.component.scss`
- Modify: `frontend/src/app/bracket/bracket-page/bracket-page.component.ts`

- [ ] **Step 1: Update bracket-page.component.ts to import new components**

```typescript
import { Component, inject, signal, OnInit, computed } from '@angular/core';
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
export class BracketPageComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private service = inject(TournamentBracketService);

  data = signal<TournamentBracketData | null>(null);
  activeTab = signal<StageTab>('bracket');
  loading = signal(true);
  error = signal<string | null>(null);

  lastWbRoundMatchCount = computed(() => {
    const wb = this.data()?.winnersBracket ?? [];
    return wb.length > 0 ? wb[wb.length - 1].matches.length : 0;
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.service.getBracket(id).subscribe({
      next: d => {
        this.data.set(d);
        this.loading.set(false);
        if (d.hasGroupStage) this.activeTab.set('groups');
      },
      error: () => {
        this.error.set('Impossible de charger le bracket.');
        this.loading.set(false);
      }
    });
  }

  onTabChange(tab: StageTab): void { this.activeTab.set(tab); }
}
```

- [ ] **Step 2: Update bracket-page.component.html**

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
        <app-bracket-layout
          [winnersBracket]="d.winnersBracket"
          [losersBracket]="d.losersBracket" />

        @if (d.grandFinal && lastWbRoundMatchCount() > 0) {
          <app-bracket-connector [matchCount]="lastWbRoundMatchCount()" />
          <app-grand-final [grandFinal]="d.grandFinal" />
        }

        @if (d.champion) {
          <app-bracket-connector [matchCount]="2" />
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

- [ ] **Step 3: Update bracket-page.component.scss**

```scss
.page {
  position: relative; z-index: 1;
  padding: 48px 24px 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow-x: auto;
}

.bracket-view {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 0;
  overflow-x: auto;
  padding-bottom: 16px;
}

.state-msg {
  font-family: 'Bebas Neue', sans-serif; font-size: 18px; letter-spacing: 4px;
  text-align: center; margin-top: 120px; color: rgba(220, 80, 80, 0.70);
  &.error { color: rgba(255, 60, 60, 0.80); }
}
```

- [ ] **Step 4: Run the dev server and verify**

```bash
cd frontend && npx ng serve
```

Open `http://localhost:4200/tournament/1/bracket`.

Verify:
- WB rounds appear left-to-right
- SVG connector appears between WB Final and GF
- GF appears after the connector
- Champion card appears at the far right (only if a champion exists)
- No TypeScript errors in browser console

- [ ] **Step 5: Commit**

```bash
git add frontend/src/app/bracket/bracket-page/bracket-page.component.html
git add frontend/src/app/bracket/bracket-page/bracket-page.component.scss
git add frontend/src/app/bracket/bracket-page/bracket-page.component.ts
git commit -m "feat(frontend): left-to-right bracket layout with SVG connectors"
```

---

## Self-Review Checklist

- ✅ **Spec §1 (nullable players)**: Tasks 1+2
- ✅ **Spec §1 (bracket_position convention)**: Tasks 2+5
- ✅ **Spec §2 (TournamentResource endpoints)**: Tasks 6+7
- ✅ **Spec §2 (PUT /matches/{id}/result)**: Tasks 8+9
- ✅ **Spec §3 (BracketGenerationService)**: Task 5
- ✅ **Spec §3 (BracketRoutingService)**: Task 4
- ✅ **Spec §4 (auto-advancement)**: Task 8
- ✅ **Spec §4 (GF champion + no bracket reset)**: Tasks 8+10
- ✅ **Spec §5 (left-to-right layout)**: Tasks 12+13+14
- ✅ **Spec §5 (SVG connectors)**: Task 12
- ✅ **Spec §5 (bracket-layout + bracket-connector)**: Tasks 12+13
- ✅ **MatchDAO LEFT JOIN FETCH fix** (required for nullable players): Task 3
- ✅ **Remove bracketReset** backend+frontend: Tasks 10+11
