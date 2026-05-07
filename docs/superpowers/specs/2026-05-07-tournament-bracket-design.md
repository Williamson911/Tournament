# Tournament Bracket System — Design Spec
**Date:** 2026-05-07  
**Stack:** Quarkus/Java (backend) + Angular (frontend)  
**Format:** Double élimination, puissances de 2 (4, 8, 16 joueurs)

---

## 1. Modèle de données

### Changements en DB

**`Match.player1` / `Match.player2` → nullable**  
Les matchs des rounds futurs sont créés dès la génération du bracket avec des slots vides. Les contraintes `nullable = false` actuelles sur `id_player1` et `id_player2` sont supprimées.

**`bracket_position` — clé métier principale**  
Convention de nommage : `{prefix}{round}{match}` où :
- `W` = Winners Bracket
- `L` = Losers Bracket
- `GF` = Grand Final (`GF1`, `GF2` pour bracket reset)

Exemple pour 8 joueurs :
```
WB : W11  W12  W13  W14  →  W21  W22  →  W31
LB : L11  L12  →  L21  L22  →  L31  →  L41
GF : GF1  (GF2 si bracket reset)
```

**`bracket_stage` et `round_number`** restent en DB mais sont **dérivés automatiquement** depuis `bracket_position` au moment de la création. Jamais saisis manuellement. Assure la compatibilité avec le `BracketService` existant.

| bracket_position prefix | bracket_stage dérivé |
|---|---|
| `W` | `WINNERS_BRACKET` |
| `L` | `LOSERS_BRACKET` |
| `GF` | `GRAND_FINAL` |

---

## 2. API Endpoints

### `TournamentResource` (nouveau)

| Méthode | URL | Description |
|---|---|---|
| `POST` | `/tournaments` | Créer un tournoi (`name`, `startDate`) → status `DRAFT` |
| `POST` | `/tournaments/{id}/registrations` | Inscrire un joueur (`playerId`) |
| `DELETE` | `/tournaments/{id}/registrations/{playerId}` | Désinscrire un joueur |
| `POST` | `/tournaments/{id}/generate-bracket` | Générer le bracket (algo double élim) |
| `PUT` | `/tournaments/{id}/status` | Changer le statut (`DRAFT→OPEN→IN_PROGRESS→FINISHED`) |

**`generate-bracket` :**
- Vérifie que le nb de joueurs confirmés est une puissance de 2 (sinon 400)
- Génère tous les matchs avec `bracket_position` calculé
- Passe le tournoi en `IN_PROGRESS`
- Retourne le `TournamentBracketDataDto` complet

### `MatchResource` (extension)

| Méthode | URL | Description |
|---|---|---|
| `PUT` | `/matches/{id}/result` | Enregistrer score + avancement automatique |

Body : `{ "player1Score": 3, "player2Score": 1, "finishType": "KO" }`

---

## 3. Algorithme de génération (double élimination)

### Structure pour 8 joueurs (14 matchs)

```
WB R1 : W11, W12, W13, W14   ← joueurs seedés aléatoirement
WB R2 : W21, W22             ← slots vides
WB Final : W31               ← slot vide

LB R1 : L11, L12             ← slots vides (losers WB R1)
LB R2 : L21, L22             ← slots vides (WB R2 losers vs LB R1 winners)
LB R3 : L31                  ← slot vide
LB Final : L41               ← slot vide

GF : GF1                     ← slot vide
```

### Table de routing (calculée algorithmiquement)

```
Gagnant W1x → W2(ceil(x/2))      Perdant W1x → L1(ceil(x/2))
Gagnant W2x → W31                Perdant W2x → L2x
Gagnant W31 → GF1 (slot1)        Perdant W31 → L41 (slot2)

Gagnant L1x → L2x               (perdant éliminé)
Gagnant L2x → L31               (perdant éliminé)
Gagnant L31 → L41               (perdant éliminé)
Gagnant L41 → GF1 (slot2)       (perdant éliminé)

Gagnant GF1 → CHAMPION
Si joueur LB gagne GF1 → créer GF2 (bracket reset) : WB player = player1, LB player = player2
Gagnant GF2 → CHAMPION
```

### Services

- **`BracketGenerationService`** — génère la structure complète de matchs depuis la liste de joueurs
- **`BracketRoutingService`** — calcule `nextWinnerPosition` et `nextLoserPosition` depuis une `bracket_position`

Chaque service a une responsabilité unique et est indépendant.

---

## 4. Enregistrement des résultats + avancement automatique

`PUT /matches/{id}/result` exécute dans l'ordre :

1. Valider que le match est `SCHEDULED` ou `IN_PROGRESS` (sinon 409 si `FINISHED`)
2. Valider que `player1` et `player2` sont non-null (sinon 400 — match pas encore jouable)
3. Valider scores non égaux (sinon 400 — pas de match nul)
4. Marquer le match `FINISHED` + `finishedAt` + `finishType`
5. Parser `bracket_position` pour déterminer gagnant/perdant
6. Via `BracketRoutingService` : calculer `nextWinnerPos` et `nextLoserPos`
7. Trouver les matchs cibles par `bracket_position` + `tournamentId`
8. Placer gagnant dans le slot libre du match suivant
9. Si WB match → placer perdant dans le slot LB correspondant
10. Si `GF1` et joueur LB gagne → créer `GF2` avec les 2 joueurs
11. Si `GF1`/`GF2` terminé → enregistrer le champion

---

## 5. Frontend — Layout gauche → droite

### Layout cible

```
┌──────────────────────────────┬──────┬──────┬──────┬──────────┐
│  WB : R1 → R2 → WB Final    │ SVG  │      │ SVG  │          │
│                              │ con. │  GF  │ con. │ Champion │
│  LB : R1 → R2 → LB Final    │      │      │      │          │
└──────────────────────────────┴──────┴──────┴──────┴──────────┘
```

### Changements de composants

**`bracket-page`**
- `.bracket-view` : `flex-direction: row; align-items: flex-start; overflow-x: auto`
- Ordre dans le template : `[bracket-layout]` → SVG connector → `[grand-final]` → SVG connector → `[champion-card]`

**Nouveau : `bracket-layout`**
- Wrapper 2 lignes (CSS grid ou flex column)
- Ligne 1 : `<app-winners-bracket>`
- Ligne 2 : `<app-losers-bracket>` (si applicable)

**Nouveau : `bracket-connector`**
- Input : `matchCount: number` (nombre de matchs dans le round précédent)
- Génère un SVG dynamique avec lignes H+V et point rouge central
- S'adapte à la hauteur réelle des matchs

**Inchangés :** `bracket-slot`, `bracket-match`, `bracket-title`, `champion-card`, `grand-final`, `bracket-round`

### Connecteurs SVG

Remplacement des connecteurs CSS pseudo-éléments actuels par des SVG dynamiques :
- Ligne H depuis le centre de chaque slot sortant
- Ligne V reliant les slots d'une même paire
- Ligne H vers le slot entrant
- Point `●` rouge au centre de jonction (`fill: rgba(230,0,0,0.75)`)

---

## Périmètre exclu

- Seeding (les joueurs sont assignés dans l'ordre d'inscription au WB R1)
- Byes (nombre de joueurs doit être exactement une puissance de 2)
- Notifications temps réel (WebSocket)
- Interface d'administration graphique (les appels API suffisent pour l'instant)
