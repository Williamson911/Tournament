# Bracket Endpoint — Backend Design

**Date:** 2026-05-06  
**Endpoint:** `GET /api/tournaments/{id}/bracket`  
**Goal:** Retourner un JSON `TournamentBracketData` pour alimenter le bracket Angular (double élimination + phase de poules optionnelle).

---

## Architecture

Pattern **Resource + Service** :

```
BracketResource  (JAX-RS, HTTP)
    └── BracketService  (assemblage DTO)
            ├── TournamentDAO
            ├── MatchDAO
            └── RegistrationDAO
```

---

## 1. Changements entité `Match`

Deux nouveaux champs :

| Champ | Type | Colonne SQL | Description |
|---|---|---|---|
| `bracketStage` | `BracketStage` (enum) | `bracket_stage` VARCHAR | Stage du match dans le tournoi |
| `roundNumber` | `int` | `round_number` INT | Numéro de ronde (0 pour GF, 1+ pour WB/LB) |

**Enum `BracketStage`** (nouveau fichier `enums/BracketStage.java`) :
```
WINNERS_BRACKET, LOSERS_BRACKET, GRAND_FINAL, GROUP_STAGE
```

`bracketPosition` (String existant) reste pour les matchs de poule : `"GROUP_A"`, `"GROUP_B"`, etc.

**Migration schema :** passer `hbm2ddl.auto` de `validate` → `update` le temps d'appliquer les colonnes, puis repasser en `validate`.

---

## 2. Couche DTO

Package `dtos/`, Java records :

```java
TournamentParticipantDto(
  int playerId, String playerName,
  String fighterName, String fighterImageUrl
)

MatchParticipantDto(                         // extends TournamentParticipant côté Angular
  int playerId, String playerName,
  String fighterName, String fighterImageUrl,
  int score, boolean isWinner, boolean isEliminated
)

BracketMatchDto(
  int matchId,
  MatchParticipantDto participant1,          // nullable
  MatchParticipantDto participant2,          // nullable
  boolean isComplete,
  boolean isBracketReset
)

BracketRoundDto(int roundId, String label, List<BracketMatchDto> matches)

TournamentGroupDto(int groupId, String name, List<GroupStandingDto> standings)

GroupStandingDto(
  int rank, TournamentParticipantDto participant,
  int wins, int losses, int points, boolean qualified
)

TournamentBracketDataDto(
  int tournamentId, String tournamentName, boolean hasGroupStage,
  List<TournamentGroupDto> groups,
  List<BracketRoundDto> winnersBracket,
  List<BracketRoundDto> losersBracket,
  BracketMatchDto grandFinal,
  BracketMatchDto bracketReset,              // nullable
  MatchParticipantDto champion               // nullable
)
```

Correspondance exacte avec les interfaces TypeScript Angular (`bracket.models.ts`).

---

## 3. BracketService — logique d'assemblage

### 3.1 Chargement
- Charger `Tournament` par id → `404` si absent
- Charger tous les `Match` du tournoi avec `player1`, `player2` (fetch eager ou JOIN FETCH)
- Charger toutes les `Registration` du tournoi pour résoudre `fighterName` + `fighterImageUrl` par joueur

### 3.2 Séparation par stage
Séparer les matchs en 4 listes selon `bracketStage` :
`WB_MATCHES`, `LB_MATCHES`, `GF_MATCH`, `GROUP_MATCHES`

### 3.3 Winners Bracket / Losers Bracket
- Grouper par `roundNumber` → trier ASC
- Label automatique par ronde :
  - `roundNumber == max` → `"FINALE"`
  - `roundNumber == max - 1` → `"DEMI-FINALE"`
  - sinon → `"ROUND N"`
  - Préfixe : `"WB "` ou `"LB "`
- `isComplete = match.status == FINISHED`

### 3.4 Grand Final
- Match unique avec `bracketStage == GRAND_FINAL`
- Champion = joueur avec le score le plus élevé si `status == FINISHED`, sinon `null`
- `bracketReset` : toujours `null` pour l'instant (pas de reset implémenté côté data)

### 3.5 Group Stage
- Grouper les matchs par `bracketPosition` (`"GROUP_A"`, `"GROUP_B"`…)
- Pour chaque groupe, calculer les standings :
  - Victoire = 3 pts, Défaite = 0 pts
  - Tri par points DESC, puis wins DESC
  - `qualified = rank <= 2`
- `hasGroupStage = !groupMatches.isEmpty()`

### 3.6 `isEliminated`
- WB : le perdant d'un match WB n'est PAS éliminé (il tombe en LB)
- LB : le perdant d'un match LB EST éliminé
- GF : le perdant EST éliminé

---

## 4. BracketResource

```java
@Path("/tournaments")
@Produces(MediaType.APPLICATION_JSON)
public class BracketResource {

    @GET
    @Path("/{id}/bracket")
    public Response getBracket(@PathParam("id") int id) {
        // appelle BracketService.buildBracketData(id)
        // retourne 200 ou 404
    }
}
```

CDI `@Inject` sur `BracketService`. `@ApplicationScoped` sur le service.

---

## 5. Fix DataInitializer

Trois corrections :

1. **`em.getTransaction().commit()`** manquant → ajouter à la fin du bloc de persistance
2. **`m.setResult(r)`** commenté → remettre le wiring bidirectionnel `Match ↔ MatchResult`
3. **Données de seed** : setter `bracketStage` + `roundNumber` sur les 3 matchs :
   - Match 1 (semi 1) : `WINNERS_BRACKET`, `roundNumber = 1`
   - Match 2 (semi 2) : `WINNERS_BRACKET`, `roundNumber = 1`
   - Match 3 (finale) : `GRAND_FINAL`, `roundNumber = 0`

---

## Fichiers à créer / modifier

**Créer :**
- `src/.../enums/BracketStage.java`
- `src/.../dtos/TournamentBracketDataDto.java`
- `src/.../dtos/BracketRoundDto.java`
- `src/.../dtos/BracketMatchDto.java`
- `src/.../dtos/MatchParticipantDto.java`
- `src/.../dtos/TournamentGroupDto.java`
- `src/.../dtos/GroupStandingDto.java`
- `src/.../services/BracketService.java`
- `src/.../resources/BracketResource.java`

**Modifier :**
- `src/.../entities/Match.java` — ajouter `bracketStage`, `roundNumber`
- `src/.../utils/DataInitializer.java` — fix commit + result wiring + seed data
- `src/main/resources/META-INF/persistence.xml` — `update` puis `validate`
