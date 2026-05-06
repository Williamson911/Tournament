# Tekken Tournament Bracket — Design Spec
**Date:** 2026-05-05
**Status:** Approved

---

## Overview

A full tournament bracket view for the Tekken tournament app. Supports two tournament formats:
- **Phase de poules + double élimination** — when the player count exceeds 32, a group stage runs first; survivors enter a double-elimination bracket.
- **Double élimination seul** — direct double-elimination bracket when ≤ 32 players.

Visual identity: Tekken 8 aesthetic — dark/red palette, oblique parallelogram fighter slots, character portraits from the wiki, champion card. Read-only view; results come from the backend.

**Reference mockup:** `.superpowers/brainstorm/3334-1777991190/content/bracket-tekken8-v3.html`

---

## Tournament Structure

### Phase de poules (> 32 joueurs)
- Players split into groups (e.g. Group A … Group H), each with 4–8 players.
- Round-robin within each group: every player faces every other player in the group.
- Standings per group: wins / losses / points / goal-average.
- Top N players per group (configurable, default: top 2) advance to the elimination bracket.
- The group stage section is hidden when player count ≤ 32.

### Double élimination
Two parallel brackets that feed into a Grand Final:

```
Winner's Bracket (WB)
  WB Round 1 → WB Quarter-Finals → WB Semi-Finals → WB Final
                                                        ↓
                                                   Grand Final ← LB Final
Loser's Bracket (LB)
  (fed by WB Round 1 losers) → LB Round 2 → … → LB Final
```

- A player who loses in the WB drops to the corresponding LB round.
- A player who loses in the LB is eliminated.
- Grand Final: WB Final winner vs LB Final winner.
- **Bracket reset:** if the LB player wins the Grand Final, a reset match is played (since the WB player hasn't lost yet). The reset match is shown as a second Grand Final slot.
- The champion card is shown once the tournament is complete.

---

## Visual Design

### Background
- Tekken 8 wallpaper (`background-size: cover`) with a fixed obsidian overlay (`rgba(4,0,0,.72)`) for readability.

### Title block
- Tournament name in Bebas Neue, large gradient (white → grey), red drop-shadow glow.
- Subtitle (e.g. "Iron Fist Tournament · Grand Prix") in smaller Bebas Neue, red-tinted.
- Decorative horizontal line below.

### Stage navigation tabs
- Two tabs: **POULES** and **BRACKET** (POULES tab hidden when ≤ 32 players).
- Tekken-style tab bar: skewed, dark background, red active indicator.
- Switching tabs is client-side only (data already loaded).

### Group stage view (POULES tab)

Each group is displayed as a card:
- Group name header (e.g. "GROUPE A") in Bebas Neue, red-tinted.
- Standings table: columns = Rank / Player / Fighter portrait thumbnail / W / L / Pts.
- Rows use the same dark/red palette as the bracket slots.
- Qualified rows (advancing to bracket) highlighted with a subtle red border-left and a "QUALIFIÉ" badge.

### Bracket view (BRACKET tab)

#### Layout
- Horizontal scroll if the bracket is too wide for the viewport.
- **Winner's Bracket** displayed on the top half of the page.
- **Loser's Bracket** displayed on the bottom half, visually separated by a labelled divider ("LOSER'S BRACKET").
- Both brackets flow left → right, with rounds as vertical columns.

#### Fighter slot
- Parallelogram shape via `transform: skewX(-12deg)`; inner content counter-skewed `skewX(12deg)`.
- `overflow: hidden` clips the portrait within the skewed bounds.
- **Winner state:** red border glow, bright red score, full portrait opacity.
- **Loser state:** `opacity: 0.42`, desaturated portrait, greyed score.
- **Eliminated state** (LB loss): same as loser but with a subtle red-X overlay on the portrait.
- **Hover state:** border and glow switch to electric blue; player name and score tint blue.
- Contents (left to right): seed rank · player name (flex-grow) · match score.
- Fighter portrait: right-aligned, counter-skewed, fades left via gradient overlay. Fighter name label at portrait bottom.
- Empty/TBD slot: placeholder with "???" player name and blank portrait area.

#### Match card
- Two slots separated by a slim skewed "VS" divider bar.
- WB matches: red accent colour.
- LB matches: same palette, slightly reduced glow intensity to visually subordinate them to WB.

#### Connector lines
- Static SVG lines in red/dark-red connecting match outputs to the next round's inputs.
- LB connectors also show the "drop" path from WB losers to their LB entry point (lighter, dashed line).
- Small circle dots at all junctions.

#### Grand Final
- Visually distinct section between WB and LB (centred, slightly larger cards).
- If a bracket reset occurs, a second match card labelled "BRACKET RESET" appears below the first.

### Champion card
- Same oblique shape as a fighter slot but taller (`min-height: 180px`).
- Background: fighter portrait at low opacity as texture.
- Contents: "Iron Fist Champion" label · player name (large red gradient) · fighter name (small, muted) · "Grand Prix Winner" sub-label.
- Hover: same blue glow as fighter slots.
- Hidden until the tournament is complete.

### Typography
- Bebas Neue — titles, labels, scores, round names, tab names.
- Rajdhani 700 — player names in slots and group table rows.
- Both loaded from Google Fonts.

### No animation
- Lightning bolts were explored and dropped. The design is static (hover transitions only).

---

## Architecture

### Component tree

```
TournamentBracketPageComponent     (route: /tournament/:id/bracket)
├── BracketTitleComponent          (title + subtitle)
├── StageTabsComponent             (POULES / BRACKET tabs; hidden if ≤ 32 players)
│
├── [POULES tab]
│   └── GroupStageComponent
│       └── GroupCardComponent × N
│           └── GroupStandingsTableComponent
│
└── [BRACKET tab]
    ├── WinnersBracketComponent
    │   ├── BracketRoundComponent × N
    │   │   └── BracketMatchComponent × N
    │   │       └── BracketSlotComponent × 2
    │   └── BracketConnectorComponent
    ├── BracketDividerComponent    ("LOSER'S BRACKET" label)
    ├── LosersBracketComponent
    │   ├── BracketRoundComponent × N
    │   │   └── BracketMatchComponent × N
    │   │       └── BracketSlotComponent × 2
    │   └── BracketConnectorComponent
    ├── GrandFinalComponent
    │   ├── BracketMatchComponent  (main GF)
    │   └── BracketMatchComponent? (bracket reset, conditional)
    └── ChampionCardComponent
```

### Data model (frontend interfaces)

```typescript
interface TournamentParticipant {
  playerId: number;
  playerName: string;
  fighterName: string;
  fighterImageUrl: string;
}

// Group stage
interface GroupStanding {
  rank: number;
  participant: TournamentParticipant;
  wins: number;
  losses: number;
  points: number;
  qualified: boolean;
}

interface TournamentGroup {
  groupId: number;
  name: string;           // "Groupe A"
  standings: GroupStanding[];
}

// Elimination bracket
interface MatchParticipant extends TournamentParticipant {
  score: number;
  isWinner: boolean;
  isEliminated: boolean;  // lost in LB
}

interface BracketMatch {
  matchId: number;
  participant1: MatchParticipant | null;  // null = TBD
  participant2: MatchParticipant | null;
  isComplete: boolean;
  isBracketReset: boolean;
}

interface BracketRound {
  roundId: number;
  label: string;          // "WB Round 1", "LB Round 2", "Grand Final", "Bracket Reset"
  matches: BracketMatch[];
}

interface TournamentBracketData {
  tournamentId: number;
  tournamentName: string;
  hasGroupStage: boolean;
  groups: TournamentGroup[];           // empty if hasGroupStage = false
  winnersBracket: BracketRound[];
  losersBracket: BracketRound[];
  grandFinal: BracketMatch;
  bracketReset: BracketMatch | null;
  champion: MatchParticipant | null;
}
```

### Data flow

```
TournamentBracketPageComponent
  → TournamentBracketService.getBracket(tournamentId): Observable<TournamentBracketData>
  → GET /api/tournaments/{id}/bracket
  → maps backend JSON → TournamentBracketData
  → passes slices down via @Input() to child components
```

### Backend endpoint (Java/Jersey — to be created)

`GET /api/tournaments/{id}/bracket`

Returns the full tournament state: groups (if applicable), all bracket rounds (WB + LB), grand final, bracket reset match, and champion. Fighter image URLs are already available in the DB from wiki data.

---

## Styling

- SCSS files per component, scoped with Angular view encapsulation.
- Shared design tokens in `src/styles/_tekken-tokens.scss`:
  - Colors: `$color-bg`, `$color-red-primary`, `$color-red-glow`, `$color-winner-border`, `$color-hover-blue`, `$color-lb-accent`
  - Fonts: `$font-display` (Bebas Neue), `$font-body` (Rajdhani)
  - Skew angle: `$slot-skew: -12deg`

---

## Out of scope

- Editing match results from the bracket view.
- Lightning animations.
- Mobile responsive layout (desktop-first for now).
- Real-time live updates (bracket is loaded once on page open).
