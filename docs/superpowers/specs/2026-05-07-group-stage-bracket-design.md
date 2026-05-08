# Group Stage + 32-Player Bracket Design

## Goal

Support a 32-player tournament that runs an auto-simulated group stage (random) to determine 16 qualifiers, then places those qualifiers into the existing double-elimination bracket.

## Architecture

Two-phase tournament lifecycle managed via tournament status transitions. No new pages or routes. Both action buttons live on the existing bracket-page.

**Tech Stack:** Quarkus/Java backend · Angular 17+ frontend (signals, standalone components)

---

## Status Flow

```
DRAFT → GROUP_STAGE_COMPLETE → IN_PROGRESS → FINISHED
```

- `DRAFT` — players registering, no bracket yet
- `GROUP_STAGE_COMPLETE` — group stage ran, 16 qualifiers identified, bracket not yet generated
- `IN_PROGRESS` — bracket generated, matches being played
- `FINISHED` — champion determined

---

## Data Model Changes

### `TournamentStatus` enum
Add `GROUP_STAGE_COMPLETE` between `DRAFT` and `IN_PROGRESS`.

### `RegistrationStatus` enum
Add `QUALIFIED`. After group stage runs, the 16 advancing players have their registration status changed from `CONFIRMED` → `QUALIFIED`. Non-qualifiers remain `CONFIRMED`.

### `Tournament` entity
Add `hasGroupStage` (boolean, default false). Set to `true` in `launchGroupStage`. Persisted in DB.

### `TournamentBracketDataDto`
Add `status` field (String, value of TournamentStatus). Used by frontend to decide which button to show.

---

## Backend

### `GroupStageService` (new)
Pure computation service, no DB access.

- Input: `List<Player>` (N players, N divisible by 4, N/2 must be a power of 2)
- Shuffles players randomly
- Splits into groups of 4
- Randomly picks 2 qualifiers per group (Collections.shuffle on each group, take first 2)
- Returns `List<Player>` of N/2 qualifiers

### `TournamentService` changes

**`launchGroupStage(int tournamentId)`**
1. Load tournament, validate status = `DRAFT`
2. Load all `CONFIRMED` registrations for the tournament
3. Validate: count divisible by 4, count/2 is a power of 2 — otherwise `BadRequestException`
4. Call `GroupStageService.selectQualifiers(players)`
5. Update qualifying registrations to status `QUALIFIED`
6. Set `tournament.hasGroupStage = true`, status → `GROUP_STAGE_COMPLETE`
7. Return `List<Player>` qualifiers

**`generateBracket(int tournamentId)`** (modified)
- If status = `GROUP_STAGE_COMPLETE`: load `QUALIFIED` registrations (already N/2, a power of 2), generate bracket, status → `IN_PROGRESS`
- If status = `DRAFT`: existing behaviour (load `CONFIRMED`, validate power of 2, generate bracket directly)
- Other statuses: `BadRequestException`

**`BracketService.buildBracketData`**
- Read `tournament.isHasGroupStage()` for the `hasGroupStage` field in the DTO instead of deriving it from GROUP_STAGE matches.

### `TournamentResource` — new endpoint

```
POST /tournaments/{id}/launch-group-stage
```
- Calls `tournamentService.launchGroupStage(id)`
- Returns `200 OK` with list of qualified players
- `@Operation(summary = "Run group stage and select qualifiers")`

### `DataInitializer`
Replace current 4-player test data with:
- 32 players (player1..player32, generic usernames)
- 1 tournament "Tekken 8 Championship" in `DRAFT` status
- 32 `CONFIRMED` registrations

---

## Frontend

### `bracket.models.ts`
Add `status: string` to `TournamentBracketData` interface.

### `tournament-bracket.service.ts`
Add two methods:
- `launchGroupStage(id: number): Observable<any>` — POST `/tournaments/{id}/launch-group-stage`
- `generateBracket(id: number): Observable<any>` — POST `/tournaments/{id}/generate-bracket`

### `bracket-page.component.ts`
- Add `launching = signal(false)` and `generating = signal(false)` for loading states
- Add `launchGroupStage()` method: calls service, then reloads bracket data via `getBracket(id)`
- Add `generateBracket()` method: calls service, then reloads bracket data via `getBracket(id)`

### `bracket-page.component.html`
Inside `.bracket-view`, show buttons conditionally:

```
data().status === 'DRAFT'                  → "Lancer les poules" button
data().status === 'GROUP_STAGE_COMPLETE'   → "Générer le bracket" button
data().status === 'IN_PROGRESS'            → bracket visible, no button
```

### `bracket-page.component.scss`
Button styled consistently with existing Tekken theme (red border, dark background, Bebas Neue font).

---

## Error Cases

| Situation | Response |
|---|---|
| `launchGroupStage` with status ≠ DRAFT | 400 Bad Request |
| `launchGroupStage` with player count not divisible by 4 | 400 Bad Request |
| `launchGroupStage` with N/2 not a power of 2 | 400 Bad Request |
| `generateBracket` with status ≠ DRAFT and ≠ GROUP_STAGE_COMPLETE | 400 Bad Request |

---

## What Does NOT Change

- `BracketGenerationService` — unchanged, already supports 16 players
- `BracketLayoutComponent`, `GrandFinalComponent`, `GroupStageComponent` — unchanged
- `BracketConnectorComponent`, `bracket-layout` — unchanged
- Existing bracket flow for non-group-stage tournaments — unchanged
