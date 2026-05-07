# Récap : tournoi 32 joueurs + simulation auto

Document destiné à expliquer les changements apportés sur la branche `willdev` mergée dans `Dev`.

## En une phrase

Passer le tournoi de 4 joueurs à 32 joueurs, avec une **phase de poule auto-simulée** qui sélectionne 16 qualifiés, **génération du bracket** double élimination existant, puis **simulation auto-live** du tournoi entier round par round.

---

## Flow utilisateur

```
DRAFT (32 inscrits)
   │  [Bouton "Lancer les poules"]
   ▼
GROUP_STAGE_COMPLETE (16 qualifiés tirés au sort)
   │  [Bouton "Générer le bracket"]
   ▼
IN_PROGRESS (matchs WB R1 placés, suite vide)
   │  [Bouton "Commencer le tournoi"]
   ▼ (animation round par round, ~800 ms entre chaque)
FINISHED (champion désigné)
```

À tout moment hors DRAFT : bouton **Reset** discret (sous le titre) qui efface les matchs, repasse les inscriptions à CONFIRMED et le tournoi à DRAFT — pratique pour rejouer sans toucher à la DB.

---

## Ce qui a changé côté backend

### Énumérations
- `TournamentStatus` : nouvelle valeur **`GROUP_STAGE_COMPLETE`** entre `DRAFT` et `IN_PROGRESS`. Indique que les poules ont été jouées mais que le bracket n'a pas encore été généré.
- `RegistrationStatus` : nouvelle valeur **`QUALIFIED`** pour les 16 joueurs qui passent les poules. Les 16 perdants gardent `CONFIRMED`.

### Entité `Tournament`
- Nouveau champ **`hasGroupStage`** (`boolean`, colonne `has_group_stage`). Vrai si le tournoi a une phase de poules. Persisté en DB pour ne pas avoir à dériver l'info des matchs (qui n'existent pas tant qu'on n'a pas généré le bracket).

### DTO `TournamentBracketDataDto`
- Nouveau champ **`status`** (String, valeur du `TournamentStatus`). Le frontend en a besoin pour décider quel bouton afficher. Sans ça, on serait obligé de faire un appel séparé à `/tournaments/{id}` pour le récupérer.

### `BracketService.buildBracketData`
- `hasGroupStage` est maintenant lu depuis `tournament.isHasGroupStage()` au lieu d'être dérivé de `!groupMatches.isEmpty()`. Raison : avec des poules auto-simulées, on ne crée pas de records `Match` pour les matchs de poule, donc l'ancienne logique aurait toujours retourné `false`.

### `RegistrationDAO`
Ajouts :
- `findByTournamentAndStatus(tournamentId, status)` — version paramétrée de `findByTournamentWithStatus` (qui était hardcodée à `CONFIRMED`). Utilisée pour charger les `QUALIFIED` au moment de générer le bracket.
- `updateStatusForPlayers(tournamentId, playerIds, status)` — bulk JPQL UPDATE. Marque 16 inscriptions comme `QUALIFIED` en une seule requête.
- `resetAllStatusForTournament(tournamentId, status)` — bulk JPQL UPDATE. Repasse toutes les inscriptions du tournoi à un statut donné (utilisé par le Reset).

### `MatchDAO`
Ajout :
- `deleteByTournament(tournamentId)` — bulk JPQL DELETE. Utilisé par le Reset pour effacer les matchs avant de retourner en DRAFT.

### `GroupStageService` *(nouveau fichier)*
Service de **calcul pur**, pas de DB. Méthode unique `selectQualifiers(List<Player>)` :
- Mélange les N joueurs aléatoirement
- Découpe en groupes de 4
- Tire 2 qualifiés au hasard par groupe
- Retourne N/2 qualifiés

C'est isolé pour pouvoir le tester unitairement (4 tests JUnit dans `GroupStageServiceTest`) sans avoir besoin de toucher la DB.

### `TournamentService`
Ajouts :
- `launchGroupStage(tournamentId)` — valide que le tournoi est en DRAFT, charge les inscriptions CONFIRMED, vérifie que le nombre est divisible par 4 et que N/2 est une puissance de 2, appelle `GroupStageService`, marque les 16 qualifiés via `updateStatusForPlayers`, met `hasGroupStage=true` et le statut à `GROUP_STAGE_COMPLETE`.
- `generateBracket(tournamentId)` modifié pour gérer **deux chemins** :
  - Si statut `DRAFT` → comportement existant (tous les CONFIRMED jouent direct)
  - Si statut `GROUP_STAGE_COMPLETE` → ne charge que les `QUALIFIED`
- `resetTournament(tournamentId)` — orchestre le Reset.

### `TournamentSimulationService` *(nouveau fichier)*
Auto-simulation du bracket round par round. Méthode `simulateNextRound(tournamentId)` :
- Charge tous les matchs **prêts** (player1 et player2 assignés, status ≠ FINISHED)
- Pour chaque match : tirage de scores aléatoires (BO3 ou BO5 selon `numberRounds`), désigne un gagnant et un perdant
- Utilise `BracketRoutingService` (déjà existant, pas modifié) pour calculer les positions cibles dans WB / LB / GF
- Place les joueurs dans les matchs suivants
- Si aucun match n'était prêt → le tournoi est terminé, statut → `FINISHED`

**Pourquoi un nouveau service plutôt que réutiliser `MatchResultService.recordResult` ?**
On a essayé. Mais après le merge avec Dev, l'entité `Match` a gagné des contraintes `nullable=false` et le fetch est passé en `EAGER` sur `player1`/`player2`/`tournament`. Conséquence : `MatchResultService.recordResult` charge un match détaché, le modifie, et fait `em.merge()`. Hibernate 7 explose alors avec une `OptimisticLockException` quand le merge cascade essaie de mettre à jour des associations qui étaient null à la création.
La solution : `TournamentSimulationService` fait **tout dans un seul `EntityManager` / une seule transaction**, en travaillant sur des entités managées (auto-flush, pas de merge détaché). Aucun risque de cascade foireux.

### `TournamentResource` — nouveaux endpoints
- `POST /tournaments/{id}/launch-group-stage` — déclenche la phase de poule
- `POST /tournaments/{id}/simulate-next-round` — simule un round de matchs prêts (le frontend l'appelle en boucle)
- `POST /tournaments/{id}/reset` — remet le tournoi en DRAFT

### `BracketGenerationService`
Une seule modif : la **Grand Final** est créée avec `numberRounds=5` (BO5, premier à 3 wins) au lieu de `numberRounds=3`. Le helper `buildMatch` a un overload pour ne pas dupliquer la logique. Justification : c'est la convention compétitive standard pour une finale de tournoi double élimination.

### `DataInitializer`
Réécrit pour seeder **32 joueurs** (player1..player32, chacun avec un fighter différent), un tournoi `Tekken 8 Championship` en `DRAFT`, et 32 inscriptions `CONFIRMED`. **Pas de matchs à l'init** : c'est le flow utilisateur qui déclenche les poules puis la génération du bracket.

L'ancien DataInitializer créait 4 joueurs et un bracket déjà à moitié joué. Plus pertinent maintenant qu'on veut pouvoir tester tout le flow depuis l'écran d'accueil.

---

## Ce qui a changé côté frontend

### `bracket.models.ts`
Ajout du champ `status: string` dans `TournamentBracketData`. Reflet du nouveau champ dans le DTO backend. Le frontend l'utilise pour savoir quel bouton afficher.

### `tournament-bracket.service.ts`
Trois nouvelles méthodes :
- `launchGroupStage(id)`
- `simulateNextRound(id)`
- `resetTournament(id)`

Et `getBracket(id)` appelle maintenant `/api/bracket/{id}/bracket` (au lieu de `/api/tournaments/{id}/bracket`) — l'endpoint a été déplacé pendant le merge avec Dev.

### `bracket-page.component.ts/html/scss`
- Trois signaux d'état : `launching`, `generating`, `simulating`, `resetting` (pour griser les boutons pendant l'opération)
- Méthodes correspondantes : `launchGroupStage()`, `generateBracket()`, `simulateTournament()` (qui démarre `simulateLoop`), `resetTournament()` (avec un `confirm()`)
- `simulateLoop()` : appelle `simulateNextRound`, met à jour les données, attend 800 ms, recommence — jusqu'à ce que le statut passe à FINISHED. C'est ce qui donne l'effet "live" de voir les matchs se résoudre round par round.
- Boutons conditionnels dans `bracket-view` selon le statut :
  - `DRAFT` → "Lancer les poules"
  - `GROUP_STAGE_COMPLETE` → "Générer le bracket"
  - `IN_PROGRESS && !champion` → "Commencer le tournoi"
- Bouton "Reset" sous le titre, visible si `status !== 'DRAFT'`

L'auto-switch de l'onglet sur "Poules" lors d'un reload a été retiré — pendant la simulation live, il faisait revenir l'utilisateur sur Poules à chaque update, ce qui cassait l'expérience.

---

## Ce qui n'est PAS dans le repo (et pourquoi)

- Pas d'animation par match individuel — le live se fait par round (tous les matchs prêts d'un coup). YAGNI : ça fait déjà bouger le bracket et c'est suffisamment lisible.
- Pas de matchs `GROUP_STAGE` créés en DB — la phase de poule est purement de la sélection aléatoire, on ne stocke que le résultat (qui passe en `QUALIFIED`). Le DTO renvoie `groups: []` après les poules. C'est volontaire : pas besoin d'afficher les poules selon le cahier des charges.
- Pas de tests pour `TournamentSimulationService` ou la nouvelle logique de `TournamentService` — ces services dépendent du DAO et de la DB, et le projet n'a pas d'infra de mocking. Seul `GroupStageService` (calcul pur) est testé unitairement. Les autres ont été validés manuellement via Swagger et le frontend.

---

## Reset DB

L'entité `Tournament` a un nouveau champ `has_group_stage`. Avec `hbm2ddl.auto=validate`, Hibernate refuse de démarrer si la colonne n'existe pas. Pour la première mise à jour il faut :
- Soit passer `hbm2ddl.auto=create` une fois (drop + recreate, perd les données), redémarrer, repasser à `validate`
- Soit faire un `ALTER TABLE tournament ADD COLUMN has_group_stage BOOLEAN DEFAULT FALSE;` à la main, ce qui préserve les données.

Une fois fait, `DataInitializer` re-seed automatiquement les 32 joueurs (parce qu'il vérifie `Fighter count == 0`).
