# Tournament Bracket — Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build la page de bracket Tekken en Angular 21 — phase de poules (optionnelle) + double élimination — alimentée par `/api/tournaments/{id}/bracket`.

**Architecture:** Composants standalone organisés sous `src/app/bracket/`. Un `TournamentBracketService` fait le fetch HTTP. La page route les données vers les composants enfants via `@Input()`. Le backend endpoint est traité dans un plan séparé — pendant le dev, la page affiche un état d'erreur géré.

**Tech Stack:** Angular 21 (standalone, signals, `@if`/`@for`), SCSS, Jasmine unit tests (`ng test`).

---

## Note de scope

Le endpoint Java/Jersey (`GET /api/tournaments/{id}/bracket`) est hors scope de ce plan. Créer un plan séparé pour la couche Jersey + service Java une fois ce plan frontend terminé.

---

## File Map

**Créer:**
- `frontend/src/styles/_tekken-tokens.scss`
- `frontend/src/app/models/bracket.models.ts`
- `frontend/src/app/services/tournament-bracket.service.ts`
- `frontend/src/app/services/tournament-bracket.service.spec.ts`
- `frontend/src/app/bracket/bracket-slot/bracket-slot.component.ts|html|scss|spec.ts`
- `frontend/src/app/bracket/bracket-match/bracket-match.component.ts|html|scss|spec.ts`
- `frontend/src/app/bracket/bracket-round/bracket-round.component.ts|html|scss`
- `frontend/src/app/bracket/winners-bracket/winners-bracket.component.ts|html|scss`
- `frontend/src/app/bracket/losers-bracket/losers-bracket.component.ts|html|scss`
- `frontend/src/app/bracket/grand-final/grand-final.component.ts|html|scss`
- `frontend/src/app/bracket/champion-card/champion-card.component.ts|html|scss`
- `frontend/src/app/bracket/bracket-title/bracket-title.component.ts|html|scss`
- `frontend/src/app/bracket/stage-tabs/stage-tabs.component.ts|html|scss`
- `frontend/src/app/bracket/group-card/group-card.component.ts|html|scss`
- `frontend/src/app/bracket/group-stage/group-stage.component.ts|html|scss`
- `frontend/src/app/bracket/bracket-page/bracket-page.component.ts|html|scss`

**Modifier:**
- `frontend/src/styles.scss` — import tokens, body background + font
- `frontend/src/app/app.config.ts` — ajouter `provideHttpClient()`
- `frontend/src/app/app.routes.ts` — ajouter la route `/tournament/:id/bracket`

---

## Task 1 — Design tokens + styles globaux

**Files:**
- Create: `frontend/src/styles/_tekken-tokens.scss`
- Modify: `frontend/src/styles.scss`

- [ ] **Étape 1 : Créer `_tekken-tokens.scss`**

```scss
// frontend/src/styles/_tekken-tokens.scss
$slot-skew: -12deg;
$slot-width: 230px;
$slot-height: 62px;

$color-bg: #040000;
$color-red-primary: rgba(220, 0, 0, 0.85);
$color-red-glow: rgba(200, 0, 0, 0.28);
$color-winner-border: rgba(220, 0, 0, 0.85);
$color-hover-blue: rgba(0, 200, 255, 0.95);
$color-hover-blue-glow: rgba(0, 160, 255, 0.55);
$color-lb-accent: rgba(120, 0, 0, 0.40);

$connector-color: rgba(180, 0, 0, 0.42);
$connector-width: 48px;

$font-display: 'Bebas Neue', sans-serif;
$font-body: 'Rajdhani', sans-serif;
```

- [ ] **Étape 2 : Mettre à jour `styles.scss`**

```scss
// frontend/src/styles.scss
@use 'styles/tekken-tokens' as t;

@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Rajdhani:wght@400;600;700&display=swap');

*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

body {
  min-height: 100vh;
  background-image: url('https://www.gamewallpapers.com/wallpapers_slechte_compressie/wallpaper_tekken_8_01_1920x1080.jpg');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  background-color: t.$color-bg;
  font-family: t.$font-body;
  color: #fff;

  &::before {
    content: '';
    position: fixed;
    inset: 0;
    background: rgba(4, 0, 0, 0.72);
    pointer-events: none;
    z-index: 0;
  }
}
```

- [ ] **Étape 3 : Vérifier la compilation**

```bash
cd frontend && npx ng build --watch=false 2>&1 | tail -5
```
Attendu : `Application bundle generation complete.`

---

## Task 2 — Data models

**Files:**
- Create: `frontend/src/app/models/bracket.models.ts`

- [ ] **Étape 1 : Écrire les interfaces**

```typescript
// frontend/src/app/models/bracket.models.ts

export interface TournamentParticipant {
  playerId: number;
  playerName: string;
  fighterName: string;
  fighterImageUrl: string;
}

export interface GroupStanding {
  rank: number;
  participant: TournamentParticipant;
  wins: number;
  losses: number;
  points: number;
  qualified: boolean;
}

export interface TournamentGroup {
  groupId: number;
  name: string;
  standings: GroupStanding[];
}

export interface MatchParticipant extends TournamentParticipant {
  score: number;
  isWinner: boolean;
  isEliminated: boolean;
}

export interface BracketMatch {
  matchId: number;
  participant1: MatchParticipant | null;
  participant2: MatchParticipant | null;
  isComplete: boolean;
  isBracketReset: boolean;
}

export interface BracketRound {
  roundId: number;
  label: string;
  matches: BracketMatch[];
}

export interface TournamentBracketData {
  tournamentId: number;
  tournamentName: string;
  hasGroupStage: boolean;
  groups: TournamentGroup[];
  winnersBracket: BracketRound[];
  losersBracket: BracketRound[];
  grandFinal: BracketMatch;
  bracketReset: BracketMatch | null;
  champion: MatchParticipant | null;
}
```

- [ ] **Étape 2 : Vérifier la compilation**

```bash
cd frontend && npx ng build --watch=false 2>&1 | tail -5
```
Attendu : `Application bundle generation complete.`

---

## Task 3 — TournamentBracketService

**Files:**
- Create: `frontend/src/app/services/tournament-bracket.service.ts`
- Create: `frontend/src/app/services/tournament-bracket.service.spec.ts`
- Modify: `frontend/src/app/app.config.ts`

- [ ] **Étape 1 : Écrire le test (TDD — doit échouer)**

```typescript
// frontend/src/app/services/tournament-bracket.service.spec.ts
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TournamentBracketService } from './tournament-bracket.service';
import { TournamentBracketData } from '../models/bracket.models';

const MOCK: TournamentBracketData = {
  tournamentId: 1,
  tournamentName: 'Iron Fist Grand Prix',
  hasGroupStage: false,
  groups: [],
  winnersBracket: [{
    roundId: 1, label: 'WB Semi-Finals',
    matches: [{
      matchId: 1,
      participant1: { playerId: 1, playerName: 'MaxCombo', fighterName: 'Jin Kazama', fighterImageUrl: '', score: 2, isWinner: true, isEliminated: false },
      participant2: { playerId: 2, playerName: 'DevilFist', fighterName: 'Kazuya', fighterImageUrl: '', score: 1, isWinner: false, isEliminated: false },
      isComplete: true, isBracketReset: false
    }]
  }],
  losersBracket: [],
  grandFinal: {
    matchId: 10,
    participant1: { playerId: 1, playerName: 'MaxCombo', fighterName: 'Jin Kazama', fighterImageUrl: '', score: 3, isWinner: true, isEliminated: false },
    participant2: { playerId: 3, playerName: 'JaggedMask', fighterName: 'King', fighterImageUrl: '', score: 2, isWinner: false, isEliminated: false },
    isComplete: true, isBracketReset: false
  },
  bracketReset: null,
  champion: { playerId: 1, playerName: 'MaxCombo', fighterName: 'Jin Kazama', fighterImageUrl: '', score: 3, isWinner: true, isEliminated: false }
};

describe('TournamentBracketService', () => {
  let service: TournamentBracketService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TournamentBracketService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(TournamentBracketService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('fetches bracket data via GET /api/tournaments/:id/bracket', () => {
    let result: TournamentBracketData | undefined;
    service.getBracket(1).subscribe(d => (result = d));
    const req = http.expectOne('/api/tournaments/1/bracket');
    expect(req.request.method).toBe('GET');
    req.flush(MOCK);
    expect(result).toEqual(MOCK);
  });
});
```

- [ ] **Étape 2 : Lancer le test — doit échouer**

```bash
cd frontend && npx ng test --watch=false 2>&1 | grep -E 'FAILED|Error|Cannot find'
```
Attendu : erreur `Cannot find module './tournament-bracket.service'`.

- [ ] **Étape 3 : Implémenter le service**

```typescript
// frontend/src/app/services/tournament-bracket.service.ts
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
}
```

- [ ] **Étape 4 : Ajouter `provideHttpClient()` dans `app.config.ts`**

```typescript
// frontend/src/app/app.config.ts
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient()
  ]
};
```

- [ ] **Étape 5 : Lancer le test — doit passer**

```bash
cd frontend && npx ng test --watch=false 2>&1 | grep -E 'Executed|FAILED|SUCCESS'
```
Attendu : `Executed 1 of 1 SUCCESS`

---

## Task 4 — Routing

**Files:**
- Modify: `frontend/src/app/app.routes.ts`

- [ ] **Étape 1 : Ajouter la route (lazy-loaded)**

```typescript
// frontend/src/app/app.routes.ts
import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'tournament/:id/bracket',
    loadComponent: () =>
      import('./bracket/bracket-page/bracket-page.component').then(m => m.BracketPageComponent)
  }
];
```

- [ ] **Étape 2 : Vérifier la compilation**

```bash
cd frontend && npx ng build --watch=false 2>&1 | tail -5
```
Attendu : `Application bundle generation complete.` (avertissement sur le composant manquant est normal à ce stade)

---

## Task 5 — BracketSlotComponent

L'unité atomique du bracket : une case parallélogramme avec portrait du fighter.

**Files:**
- Create: `frontend/src/app/bracket/bracket-slot/bracket-slot.component.ts|html|scss|spec.ts`

- [ ] **Étape 1 : Écrire les tests (TDD)**

```typescript
// frontend/src/app/bracket/bracket-slot/bracket-slot.component.spec.ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BracketSlotComponent } from './bracket-slot.component';
import { MatchParticipant } from '../../models/bracket.models';

const WINNER: MatchParticipant = {
  playerId: 1, playerName: 'MaxCombo', fighterName: 'Jin Kazama',
  fighterImageUrl: 'https://example.com/jin.jpg',
  score: 2, isWinner: true, isEliminated: false
};
const LOSER: MatchParticipant = {
  playerId: 2, playerName: 'DevilFist', fighterName: 'Kazuya',
  fighterImageUrl: '', score: 1, isWinner: false, isEliminated: false
};
const ELIMINATED: MatchParticipant = {
  playerId: 3, playerName: 'Silencer', fighterName: 'Nina',
  fighterImageUrl: '', score: 0, isWinner: false, isEliminated: true
};

describe('BracketSlotComponent', () => {
  let fixture: ComponentFixture<BracketSlotComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [BracketSlotComponent] });
    fixture = TestBed.createComponent(BracketSlotComponent);
  });

  it('affiche le nom du joueur et le score', () => {
    fixture.componentRef.setInput('participant', WINNER);
    fixture.componentRef.setInput('seed', 1);
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.slot-name')!.textContent!.trim()).toBe('MaxCombo');
    expect(el.querySelector('.slot-score')!.textContent!.trim()).toBe('2');
  });

  it('applique .winner au gagnant', () => {
    fixture.componentRef.setInput('participant', WINNER);
    fixture.componentRef.setInput('seed', 1);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.slot-wrapper').classList).toContain('winner');
  });

  it('applique .loser au perdant non éliminé', () => {
    fixture.componentRef.setInput('participant', LOSER);
    fixture.componentRef.setInput('seed', 2);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.slot-wrapper').classList).toContain('loser');
  });

  it('applique .eliminated au joueur éliminé', () => {
    fixture.componentRef.setInput('participant', ELIMINATED);
    fixture.componentRef.setInput('seed', 3);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.slot-wrapper').classList).toContain('eliminated');
  });

  it('affiche ??? quand participant est null (slot TBD)', () => {
    fixture.componentRef.setInput('participant', null);
    fixture.componentRef.setInput('seed', '');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.slot-name')!.textContent!.trim()).toBe('???');
  });

  it('affiche le nom du fighter sur le portrait', () => {
    fixture.componentRef.setInput('participant', WINNER);
    fixture.componentRef.setInput('seed', 1);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.portrait-label')!.textContent!.trim()).toBe('Jin Kazama');
  });
});
```

- [ ] **Étape 2 : Lancer — doit échouer**

```bash
cd frontend && npx ng test --watch=false 2>&1 | grep -E 'FAILED|Cannot find'
```

- [ ] **Étape 3 : Implémenter le composant**

```typescript
// frontend/src/app/bracket/bracket-slot/bracket-slot.component.ts
import { Component, input } from '@angular/core';
import { MatchParticipant } from '../../models/bracket.models';

@Component({
  selector: 'app-bracket-slot',
  templateUrl: './bracket-slot.component.html',
  styleUrl: './bracket-slot.component.scss'
})
export class BracketSlotComponent {
  participant = input<MatchParticipant | null>(null);
  seed = input<number | string>('');
}
```

```html
<!-- frontend/src/app/bracket/bracket-slot/bracket-slot.component.html -->
<div class="slot-wrapper"
     [class.winner]="participant()?.isWinner"
     [class.loser]="participant() !== null && !participant()!.isWinner && !participant()!.isEliminated"
     [class.eliminated]="participant()?.isEliminated">
  <div class="slot-portrait">
    @if (participant()) {
      <img [src]="participant()!.fighterImageUrl" [alt]="participant()!.fighterName" />
      <div class="portrait-label">{{ participant()!.fighterName }}</div>
    }
  </div>
  <div class="slot-content">
    <span class="slot-rank">{{ seed() }}</span>
    <span class="slot-name">{{ participant()?.playerName ?? '???' }}</span>
    <span class="slot-score">{{ participant()?.score ?? '' }}</span>
  </div>
</div>
```

```scss
// frontend/src/app/bracket/bracket-slot/bracket-slot.component.scss
@use '../../../styles/tekken-tokens' as t;

.slot-wrapper {
  position: relative;
  width: t.$slot-width;
  height: t.$slot-height;
  transform: skewX(t.$slot-skew);
  border: 1px solid rgba(130, 20, 20, 0.55);
  background: linear-gradient(135deg, rgba(12, 0, 0, 0.97) 0%, rgba(6, 0, 0, 0.97) 100%);
  overflow: hidden;
  transition: transform 0.15s, border-color 0.15s, box-shadow 0.15s;
  margin: 2px 0;
  cursor: pointer;

  &::before {
    content: '';
    position: absolute;
    left: 0; top: 0; bottom: 0;
    width: 3px;
    background: rgba(100, 0, 0, 0.40);
    z-index: 4;
  }

  &.winner {
    background: linear-gradient(135deg, rgba(35, 0, 0, 0.98) 0%, rgba(18, 0, 0, 0.98) 100%);
    border-color: rgba(220, 0, 0, 0.85);
    box-shadow: 0 0 18px rgba(200, 0, 0, 0.28), inset 0 0 24px rgba(120, 0, 0, 0.12), -3px 0 0 rgba(220, 0, 0, 0.55);

    &::before { background: linear-gradient(180deg, rgba(255, 60, 60, 0.80), rgba(180, 0, 0, 0.60)); }

    &::after {
      content: '';
      position: absolute;
      top: 0; left: 0; right: 0;
      height: 2px;
      z-index: 4;
      background: linear-gradient(90deg, rgba(180, 0, 0, 0.5), rgba(255, 70, 70, 0.9), rgba(200, 30, 30, 0.7), transparent);
      box-shadow: 0 0 8px rgba(255, 0, 0, 0.6);
    }

    .slot-name { color: #fff; text-shadow: 0 0 8px rgba(255, 80, 80, 0.35); }
    .slot-score { color: #ff2222; text-shadow: 0 0 12px rgba(255, 0, 0, 0.65); }
  }

  &.loser { opacity: 0.42; }

  &.eliminated {
    opacity: 0.42;
    &::after {
      content: '✕';
      position: absolute;
      top: 50%; right: 12px;
      transform: translateY(-50%) skewX(12deg);
      font-size: 18px;
      color: rgba(220, 0, 0, 0.45);
      z-index: 5;
    }
  }

  &:hover {
    transform: skewX(t.$slot-skew) scale(1.035);
    border-color: t.$color-hover-blue !important;
    box-shadow: 0 0 28px t.$color-hover-blue-glow, 0 0 55px rgba(0, 90, 255, 0.30), inset 0 0 18px rgba(0, 60, 200, 0.18) !important;
    z-index: 10;

    &::before { background: rgba(0, 180, 255, 0.55); }
    .slot-name { color: #80ddff !important; }
    .slot-score { color: #00ddff !important; }
  }
}

.slot-portrait {
  position: absolute;
  right: -6px; top: 0; bottom: 0;
  width: 88px;
  overflow: hidden;
  z-index: 1;

  img {
    height: 135%; width: 100%;
    object-fit: cover; object-position: top center;
    opacity: 0.78;
    filter: saturate(0.75) contrast(1.25) brightness(0.88);
    transform: skewX(12deg) translateX(-6px) scale(1.08);
    transform-origin: top right;
    transition: opacity 0.2s, filter 0.2s;
  }

  &::before {
    content: '';
    position: absolute; inset: 0; z-index: 2;
    background:
      linear-gradient(180deg, transparent 55%, rgba(6, 0, 0, 0.70) 100%),
      linear-gradient(90deg, rgba(10, 0, 0, 0.90) 0%, rgba(8, 0, 0, 0.50) 42%, rgba(4, 0, 0, 0.08) 100%);
  }
}

.portrait-label {
  position: absolute;
  bottom: 4px; left: 0; right: 0;
  z-index: 3;
  text-align: center;
  transform: skewX(12deg);
  padding: 0 3px;
  font-family: t.$font-display;
  font-size: 9px; letter-spacing: 1px;
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.95);
  white-space: nowrap; overflow: hidden; line-height: 1;
}

.slot-content {
  transform: skewX(12deg);
  height: 100%;
  display: flex; align-items: center;
  gap: 10px; padding: 0 12px 0 16px;
  position: relative; z-index: 2;
}

.slot-rank {
  font-family: t.$font-display; font-size: 10px;
  color: rgba(200, 70, 70, 0.55);
  min-width: 14px; text-align: center;
}

.slot-name {
  flex: 1; font-size: 12px; font-weight: 700;
  letter-spacing: 2px; text-transform: uppercase;
  color: #ccc; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  transition: color 0.15s;
}

.slot-score {
  font-family: t.$font-display; font-size: 26px;
  min-width: 22px; text-align: center; line-height: 1;
  color: #2a0a0a; transition: color 0.15s;
}
```

- [ ] **Étape 4 : Lancer les tests — doivent passer**

```bash
cd frontend && npx ng test --watch=false 2>&1 | grep -E 'Executed|FAILED|SUCCESS'
```
Attendu : `Executed 7 of 7 SUCCESS`

---

## Task 6 — BracketMatchComponent

Deux slots + barre VS.

**Files:**
- Create: `frontend/src/app/bracket/bracket-match/bracket-match.component.ts|html|scss|spec.ts`

- [ ] **Étape 1 : Écrire les tests**

```typescript
// frontend/src/app/bracket/bracket-match/bracket-match.component.spec.ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BracketMatchComponent } from './bracket-match.component';
import { BracketMatch } from '../../models/bracket.models';

const MATCH: BracketMatch = {
  matchId: 1,
  participant1: { playerId: 1, playerName: 'MaxCombo', fighterName: 'Jin Kazama', fighterImageUrl: '', score: 2, isWinner: true, isEliminated: false },
  participant2: { playerId: 2, playerName: 'DevilFist', fighterName: 'Kazuya', fighterImageUrl: '', score: 1, isWinner: false, isEliminated: false },
  isComplete: true, isBracketReset: false
};

const TBD: BracketMatch = {
  matchId: 2, participant1: null, participant2: null,
  isComplete: false, isBracketReset: false
};

describe('BracketMatchComponent', () => {
  let fixture: ComponentFixture<BracketMatchComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [BracketMatchComponent] });
    fixture = TestBed.createComponent(BracketMatchComponent);
  });

  it('rend deux app-bracket-slot', () => {
    fixture.componentRef.setInput('match', MATCH);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('app-bracket-slot').length).toBe(2);
  });

  it('rend la barre VS', () => {
    fixture.componentRef.setInput('match', MATCH);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.vs-divider')).toBeTruthy();
  });

  it('fonctionne avec participants null (TBD)', () => {
    fixture.componentRef.setInput('match', TBD);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('app-bracket-slot').length).toBe(2);
  });
});
```

- [ ] **Étape 2 : Lancer — doit échouer**

```bash
cd frontend && npx ng test --watch=false 2>&1 | grep -E 'FAILED|Cannot find'
```

- [ ] **Étape 3 : Implémenter**

```typescript
// frontend/src/app/bracket/bracket-match/bracket-match.component.ts
import { Component, input } from '@angular/core';
import { BracketSlotComponent } from '../bracket-slot/bracket-slot.component';
import { BracketMatch } from '../../models/bracket.models';

@Component({
  selector: 'app-bracket-match',
  imports: [BracketSlotComponent],
  templateUrl: './bracket-match.component.html',
  styleUrl: './bracket-match.component.scss'
})
export class BracketMatchComponent {
  match = input.required<BracketMatch>();
}
```

```html
<!-- frontend/src/app/bracket/bracket-match/bracket-match.component.html -->
<div class="match">
  <app-bracket-slot [participant]="match().participant1" [seed]="1" />
  <div class="vs-divider">VS</div>
  <app-bracket-slot [participant]="match().participant2" [seed]="2" />
</div>
```

```scss
// frontend/src/app/bracket/bracket-match/bracket-match.component.scss
@use '../../../styles/tekken-tokens' as t;

.match { display: flex; flex-direction: column; position: relative; }

.vs-divider {
  width: t.$slot-width;
  transform: skewX(t.$slot-skew);
  text-align: right;
  font-family: t.$font-display;
  font-size: 8px; letter-spacing: 3px;
  color: rgba(180, 50, 50, 0.35);
  padding: 3px 16px;
  background: rgba(15, 0, 0, 0.85);
  border-top: 1px solid rgba(90, 0, 0, 0.28);
  border-bottom: 1px solid rgba(90, 0, 0, 0.28);
}
```

- [ ] **Étape 4 : Lancer les tests — doivent passer**

```bash
cd frontend && npx ng test --watch=false 2>&1 | grep -E 'Executed|FAILED|SUCCESS'
```
Attendu : `Executed 10 of 10 SUCCESS`

---

## Task 7 — BracketRoundComponent

Colonne de matchs avec label de ronde.

**Files:**
- Create: `frontend/src/app/bracket/bracket-round/bracket-round.component.ts|html|scss`

- [ ] **Étape 1 : Implémenter**

```typescript
// frontend/src/app/bracket/bracket-round/bracket-round.component.ts
import { Component, input } from '@angular/core';
import { BracketMatchComponent } from '../bracket-match/bracket-match.component';
import { BracketRound } from '../../models/bracket.models';

@Component({
  selector: 'app-bracket-round',
  imports: [BracketMatchComponent],
  templateUrl: './bracket-round.component.html',
  styleUrl: './bracket-round.component.scss'
})
export class BracketRoundComponent {
  round = input.required<BracketRound>();
}
```

```html
<!-- frontend/src/app/bracket/bracket-round/bracket-round.component.html -->
<div class="round">
  <div class="round-label">{{ round().label }}</div>
  <div class="matches">
    @for (match of round().matches; track match.matchId) {
      <app-bracket-match [match]="match" />
    }
  </div>
</div>
```

```scss
// frontend/src/app/bracket/bracket-round/bracket-round.component.scss
@use '../../../styles/tekken-tokens' as t;

.round { display: flex; flex-direction: column; align-items: center; }

.round-label {
  font-family: t.$font-display; font-size: 11px; letter-spacing: 5px;
  color: rgba(220, 80, 80, 0.75);
  margin-bottom: 18px; text-align: center;
  text-shadow: 0 0 8px rgba(200, 0, 0, 0.40), 0 2px 5px rgba(0, 0, 0, 0.90);
}

.matches { display: flex; flex-direction: column; gap: 24px; }
```

- [ ] **Étape 2 : Vérifier**

```bash
cd frontend && npx ng build --watch=false 2>&1 | tail -5
```
Attendu : `Application bundle generation complete.`

---

## Task 8 — WinnersBracketComponent

Rondes WB en ligne avec connecteurs CSS entre rondes.

**Files:**
- Create: `frontend/src/app/bracket/winners-bracket/winners-bracket.component.ts|html|scss`

- [ ] **Étape 1 : Implémenter**

```typescript
// frontend/src/app/bracket/winners-bracket/winners-bracket.component.ts
import { Component, input } from '@angular/core';
import { BracketRoundComponent } from '../bracket-round/bracket-round.component';
import { BracketRound } from '../../models/bracket.models';

@Component({
  selector: 'app-winners-bracket',
  imports: [BracketRoundComponent],
  templateUrl: './winners-bracket.component.html',
  styleUrl: './winners-bracket.component.scss'
})
export class WinnersBracketComponent {
  rounds = input.required<BracketRound[]>();
}
```

```html
<!-- frontend/src/app/bracket/winners-bracket/winners-bracket.component.html -->
<section class="wb-section">
  <div class="section-label">WINNER'S BRACKET</div>
  <div class="rounds-row">
    @for (round of rounds(); track round.roundId; let last = $last) {
      <app-bracket-round [round]="round" />
      @if (!last) {
        <div class="connector-col">
          @for (match of round.matches; track match.matchId; let i = $index) {
            @if (i % 2 === 0) {
              <div class="connector-pair"></div>
            }
          }
        </div>
      }
    }
  </div>
</section>
```

```scss
// frontend/src/app/bracket/winners-bracket/winners-bracket.component.scss
@use '../../../styles/tekken-tokens' as t;

.wb-section { margin-bottom: 48px; }

.section-label {
  font-family: t.$font-display; font-size: 13px; letter-spacing: 6px;
  color: rgba(220, 80, 80, 0.60);
  margin-bottom: 24px; padding-left: 8px;
  border-left: 2px solid rgba(180, 0, 0, 0.50);
}

.rounds-row {
  display: flex; align-items: center; gap: 0; overflow-x: auto;
}

.connector-col {
  display: flex; flex-direction: column; justify-content: space-around;
  width: t.$connector-width; align-self: stretch;
}

.connector-pair {
  position: relative; flex: 1; min-height: 80px;

  // vertical line joining two match outputs
  &::before {
    content: ''; position: absolute;
    right: 0; top: 25%; bottom: 25%;
    width: 1px; background: t.$connector-color;
  }

  // horizontal line going right to next round
  &::after {
    content: ''; position: absolute;
    right: 0; top: 50%;
    width: 100%; height: 1px;
    background: t.$connector-color;
  }
}
```

- [ ] **Étape 2 : Vérifier**

```bash
cd frontend && npx ng build --watch=false 2>&1 | tail -5
```

---

## Task 9 — LosersBracketComponent

Même structure que WB, palette légèrement atténuée.

**Files:**
- Create: `frontend/src/app/bracket/losers-bracket/losers-bracket.component.ts|html|scss`

- [ ] **Étape 1 : Implémenter**

```typescript
// frontend/src/app/bracket/losers-bracket/losers-bracket.component.ts
import { Component, input } from '@angular/core';
import { BracketRoundComponent } from '../bracket-round/bracket-round.component';
import { BracketRound } from '../../models/bracket.models';

@Component({
  selector: 'app-losers-bracket',
  imports: [BracketRoundComponent],
  templateUrl: './losers-bracket.component.html',
  styleUrl: './losers-bracket.component.scss'
})
export class LosersBracketComponent {
  rounds = input.required<BracketRound[]>();
}
```

```html
<!-- frontend/src/app/bracket/losers-bracket/losers-bracket.component.html -->
<section class="lb-section">
  <div class="section-label">LOSER'S BRACKET</div>
  <div class="rounds-row">
    @for (round of rounds(); track round.roundId; let last = $last) {
      <div class="lb-round">
        <app-bracket-round [round]="round" />
      </div>
      @if (!last) {
        <div class="connector-col">
          @for (match of round.matches; track match.matchId; let i = $index) {
            @if (i % 2 === 0) {
              <div class="connector-pair"></div>
            }
          }
        </div>
      }
    }
  </div>
</section>
```

```scss
// frontend/src/app/bracket/losers-bracket/losers-bracket.component.scss
@use '../../../styles/tekken-tokens' as t;

.lb-section { margin-top: 8px; }

.section-label {
  font-family: t.$font-display; font-size: 13px; letter-spacing: 6px;
  color: rgba(160, 40, 40, 0.55);
  margin-bottom: 24px; padding-left: 8px;
  border-left: 2px solid rgba(120, 0, 0, 0.40);
}

.rounds-row { display: flex; align-items: center; gap: 0; overflow-x: auto; }

.connector-col {
  display: flex; flex-direction: column; justify-content: space-around;
  width: t.$connector-width; align-self: stretch;
}

.connector-pair {
  position: relative; flex: 1; min-height: 80px;

  &::before {
    content: ''; position: absolute;
    right: 0; top: 25%; bottom: 25%;
    width: 1px; background: rgba(120, 0, 0, 0.35);
  }

  &::after {
    content: ''; position: absolute;
    right: 0; top: 50%;
    width: 100%; height: 1px;
    background: rgba(120, 0, 0, 0.35);
  }
}
```

- [ ] **Étape 2 : Vérifier**

```bash
cd frontend && npx ng build --watch=false 2>&1 | tail -5
```

---

## Task 10 — GrandFinalComponent

Match de Grande Finale + bracket reset conditionnel.

**Files:**
- Create: `frontend/src/app/bracket/grand-final/grand-final.component.ts|html|scss`

- [ ] **Étape 1 : Implémenter**

```typescript
// frontend/src/app/bracket/grand-final/grand-final.component.ts
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
  bracketReset = input<BracketMatch | null>(null);
}
```

```html
<!-- frontend/src/app/bracket/grand-final/grand-final.component.html -->
<div class="gf-section">
  <div class="gf-label">GRAND FINAL</div>
  <app-bracket-match [match]="grandFinal()" />
  @if (bracketReset()) {
    <div class="reset-label">BRACKET RESET</div>
    <app-bracket-match [match]="bracketReset()!" />
  }
</div>
```

```scss
// frontend/src/app/bracket/grand-final/grand-final.component.scss
@use '../../../styles/tekken-tokens' as t;

.gf-section { display: flex; flex-direction: column; align-items: center; padding: 0 32px; }

.gf-label {
  font-family: t.$font-display; font-size: 14px; letter-spacing: 7px;
  color: rgba(255, 80, 80, 0.85); margin-bottom: 18px; text-align: center;
  text-shadow: 0 0 12px rgba(220, 0, 0, 0.55), 0 2px 6px rgba(0, 0, 0, 0.90);
}

.reset-label {
  font-family: t.$font-display; font-size: 10px; letter-spacing: 5px;
  color: rgba(200, 60, 60, 0.65);
  margin: 20px 0 12px; padding-top: 16px;
  border-top: 1px solid rgba(140, 0, 0, 0.30);
  width: 100%; text-align: center;
}
```

- [ ] **Étape 2 : Vérifier**

```bash
cd frontend && npx ng build --watch=false 2>&1 | tail -5
```

---

## Task 11 — ChampionCardComponent

**Files:**
- Create: `frontend/src/app/bracket/champion-card/champion-card.component.ts|html|scss`

- [ ] **Étape 1 : Implémenter**

```typescript
// frontend/src/app/bracket/champion-card/champion-card.component.ts
import { Component, input } from '@angular/core';
import { MatchParticipant } from '../../models/bracket.models';

@Component({
  selector: 'app-champion-card',
  templateUrl: './champion-card.component.html',
  styleUrl: './champion-card.component.scss'
})
export class ChampionCardComponent {
  champion = input.required<MatchParticipant>();
}
```

```html
<!-- frontend/src/app/bracket/champion-card/champion-card.component.html -->
<div class="champion-card">
  <div class="champion-portrait">
    <img [src]="champion().fighterImageUrl" [alt]="champion().fighterName" />
  </div>
  <div class="champion-inner">
    <div class="champion-label">Iron Fist Champion</div>
    <div class="champion-player">{{ champion().playerName }}</div>
    <span class="champion-fighter">{{ champion().fighterName }}</span>
    <div class="champion-sub">Grand Prix Winner</div>
  </div>
</div>
```

```scss
// frontend/src/app/bracket/champion-card/champion-card.component.scss
@use '../../../styles/tekken-tokens' as t;

.champion-card {
  position: relative; width: 200px; min-height: 180px;
  transform: skewX(t.$slot-skew);
  background: linear-gradient(135deg, rgba(38, 0, 0, 0.99) 0%, rgba(14, 0, 0, 0.99) 100%);
  border: 1px solid rgba(200, 0, 0, 0.72);
  box-shadow: 0 0 50px rgba(200, 0, 0, 0.22), inset 0 0 35px rgba(90, 0, 0, 0.10), -4px 0 0 rgba(220, 0, 0, 0.62);
  overflow: hidden; cursor: pointer;
  transition: transform 0.15s, border-color 0.15s, box-shadow 0.15s;

  &::before {
    content: ''; position: absolute; top: 0; left: 0; right: 0; height: 2px; z-index: 4;
    background: linear-gradient(90deg, rgba(160, 0, 0, 0.5), rgba(255, 50, 50, 0.95), rgba(220, 0, 0, 0.7), transparent);
    box-shadow: 0 0 14px rgba(255, 0, 0, 0.80);
  }

  &:hover {
    transform: skewX(t.$slot-skew) scale(1.04);
    border-color: t.$color-hover-blue !important;
    box-shadow: 0 0 40px t.$color-hover-blue-glow, inset 0 0 20px rgba(0, 60, 200, 0.18) !important;
  }
}

.champion-portrait {
  position: absolute; inset: 0; overflow: hidden;

  img {
    width: 110%; height: 100%; object-fit: cover; object-position: top center;
    opacity: 0.28; filter: saturate(0.55) contrast(1.35);
    transform: skewX(12deg) translateX(-5%) scale(1.1); transform-origin: top right;
  }

  &::before {
    content: ''; position: absolute; inset: 0; z-index: 1;
    background: linear-gradient(180deg, rgba(20, 0, 0, 0.25) 0%, rgba(10, 0, 0, 0.85) 100%);
  }
}

.champion-inner {
  transform: skewX(12deg); position: relative; z-index: 2;
  padding: 20px 16px 20px 20px; text-align: center;
}

.champion-label {
  font-family: t.$font-display; font-size: 10px; letter-spacing: 6px;
  color: rgba(210, 80, 80, 0.60); margin-bottom: 8px;
}

.champion-player {
  font-family: t.$font-display; font-size: 26px; letter-spacing: 2px; line-height: 1.05;
  background: linear-gradient(180deg, #fff 0%, #ffbbbb 35%, #ff3333 75%, #cc0000 100%);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
  filter: drop-shadow(0 0 10px rgba(200, 0, 0, 0.55));
}

.champion-fighter {
  font-family: t.$font-display; font-size: 11px; letter-spacing: 3px;
  color: rgba(200, 140, 140, 0.75); margin-top: 4px;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.9); display: block;
}

.champion-sub {
  font-size: 9px; letter-spacing: 3px;
  color: rgba(200, 70, 70, 0.50); margin-top: 8px; text-transform: uppercase;
}
```

- [ ] **Étape 2 : Vérifier**

```bash
cd frontend && npx ng build --watch=false 2>&1 | tail -5
```

---

## Task 12 — BracketTitleComponent + StageTabsComponent

**Files:**
- Create: `frontend/src/app/bracket/bracket-title/bracket-title.component.ts|html|scss`
- Create: `frontend/src/app/bracket/stage-tabs/stage-tabs.component.ts|html|scss`

- [ ] **Étape 1 : BracketTitleComponent**

```typescript
// frontend/src/app/bracket/bracket-title/bracket-title.component.ts
import { Component, input } from '@angular/core';

@Component({
  selector: 'app-bracket-title',
  templateUrl: './bracket-title.component.html',
  styleUrl: './bracket-title.component.scss'
})
export class BracketTitleComponent {
  title = input.required<string>();
  subtitle = input<string>('');
}
```

```html
<!-- frontend/src/app/bracket/bracket-title/bracket-title.component.html -->
<div class="title-block">
  <div class="title-main">{{ title() }}</div>
  @if (subtitle()) { <div class="title-sub">{{ subtitle() }}</div> }
  <div class="title-line"></div>
</div>
```

```scss
// frontend/src/app/bracket/bracket-title/bracket-title.component.scss
@use '../../../styles/tekken-tokens' as t;

.title-block { text-align: center; margin-bottom: 60px; }

.title-main {
  font-family: t.$font-display;
  font-size: clamp(64px, 10vw, 128px); letter-spacing: 12px; line-height: 0.95;
  background: linear-gradient(180deg, #fff 0%, #eee 20%, #ccc 55%, #999 85%, #555 100%);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
  filter:
    drop-shadow(0 0 50px rgba(220, 0, 0, 0.70))
    drop-shadow(0 0 20px rgba(160, 0, 0, 0.55))
    drop-shadow(2px 4px 0 rgba(0, 0, 0, 0.95));
}

.title-sub {
  font-family: t.$font-display; font-size: clamp(11px, 1.8vw, 15px); letter-spacing: 14px;
  color: rgba(220, 80, 80, 0.80); margin-top: 4px;
  text-shadow: 0 0 12px rgba(220, 0, 0, 0.50), 0 2px 6px rgba(0, 0, 0, 0.90);
}

.title-line {
  width: 340px; height: 1px; margin: 16px auto 0;
  background: linear-gradient(90deg, transparent, rgba(180, 0, 0, 0.7), rgba(255, 60, 60, 0.9), rgba(180, 0, 0, 0.7), transparent);
  box-shadow: 0 0 10px rgba(200, 0, 0, 0.55);
}
```

- [ ] **Étape 2 : StageTabsComponent**

```typescript
// frontend/src/app/bracket/stage-tabs/stage-tabs.component.ts
import { Component, input, output } from '@angular/core';

export type StageTab = 'groups' | 'bracket';

@Component({
  selector: 'app-stage-tabs',
  templateUrl: './stage-tabs.component.html',
  styleUrl: './stage-tabs.component.scss'
})
export class StageTabsComponent {
  activeTab = input.required<StageTab>();
  tabChange = output<StageTab>();
}
```

```html
<!-- frontend/src/app/bracket/stage-tabs/stage-tabs.component.html -->
<div class="tabs">
  <button class="tab" [class.active]="activeTab() === 'groups'" (click)="tabChange.emit('groups')">POULES</button>
  <button class="tab" [class.active]="activeTab() === 'bracket'" (click)="tabChange.emit('bracket')">BRACKET</button>
</div>
```

```scss
// frontend/src/app/bracket/stage-tabs/stage-tabs.component.scss
@use '../../../styles/tekken-tokens' as t;

.tabs {
  display: flex; gap: 0; margin-bottom: 40px;
  border-bottom: 1px solid rgba(140, 0, 0, 0.35);
}

.tab {
  font-family: t.$font-display; font-size: 14px; letter-spacing: 4px;
  padding: 12px 32px;
  background: rgba(8, 0, 0, 0.85); border: none;
  border-bottom: 2px solid transparent;
  color: rgba(180, 60, 60, 0.65);
  cursor: pointer; transform: skewX(-8deg);
  transition: color 0.15s, border-color 0.15s, background 0.15s;

  &.active {
    color: #fff; border-bottom-color: rgba(220, 0, 0, 0.90);
    background: rgba(30, 0, 0, 0.90);
    text-shadow: 0 0 10px rgba(220, 0, 0, 0.45);
  }

  &:hover:not(.active) { color: rgba(220, 100, 100, 0.90); background: rgba(18, 0, 0, 0.90); }
}
```

- [ ] **Étape 3 : Vérifier**

```bash
cd frontend && npx ng build --watch=false 2>&1 | tail -5
```

---

## Task 13 — GroupCardComponent

Table de classement d'un groupe.

**Files:**
- Create: `frontend/src/app/bracket/group-card/group-card.component.ts|html|scss`

- [ ] **Étape 1 : Implémenter**

```typescript
// frontend/src/app/bracket/group-card/group-card.component.ts
import { Component, input } from '@angular/core';
import { TournamentGroup } from '../../models/bracket.models';

@Component({
  selector: 'app-group-card',
  templateUrl: './group-card.component.html',
  styleUrl: './group-card.component.scss'
})
export class GroupCardComponent {
  group = input.required<TournamentGroup>();
}
```

```html
<!-- frontend/src/app/bracket/group-card/group-card.component.html -->
<div class="group-card">
  <div class="group-name">{{ group().name }}</div>
  <table class="standings">
    <thead>
      <tr>
        <th>#</th><th>Joueur</th><th>Fighter</th><th>V</th><th>D</th><th>Pts</th><th></th>
      </tr>
    </thead>
    <tbody>
      @for (row of group().standings; track row.participant.playerId) {
        <tr [class.qualified]="row.qualified">
          <td class="rank">{{ row.rank }}</td>
          <td class="player-name">{{ row.participant.playerName }}</td>
          <td class="fighter-name">{{ row.participant.fighterName }}</td>
          <td>{{ row.wins }}</td>
          <td>{{ row.losses }}</td>
          <td class="pts">{{ row.points }}</td>
          <td>@if (row.qualified) { <span class="badge">QUALIFIÉ</span> }</td>
        </tr>
      }
    </tbody>
  </table>
</div>
```

```scss
// frontend/src/app/bracket/group-card/group-card.component.scss
@use '../../../styles/tekken-tokens' as t;

.group-card {
  background: linear-gradient(135deg, rgba(14, 0, 0, 0.95) 0%, rgba(6, 0, 0, 0.95) 100%);
  border: 1px solid rgba(110, 10, 10, 0.50);
  min-width: 360px; overflow: hidden;
}

.group-name {
  font-family: t.$font-display; font-size: 13px; letter-spacing: 6px;
  color: rgba(220, 80, 80, 0.85);
  padding: 12px 16px;
  background: rgba(30, 0, 0, 0.80);
  border-bottom: 1px solid rgba(140, 0, 0, 0.35);
  text-shadow: 0 0 8px rgba(200, 0, 0, 0.40);
}

.standings {
  width: 100%; border-collapse: collapse;

  th {
    font-family: t.$font-display; font-size: 9px; letter-spacing: 3px;
    color: rgba(180, 60, 60, 0.60);
    padding: 8px 12px; text-align: left;
    border-bottom: 1px solid rgba(90, 0, 0, 0.25);
  }

  td {
    font-family: t.$font-body; font-size: 13px; font-weight: 600;
    padding: 9px 12px; color: rgba(200, 180, 180, 0.80);
    border-bottom: 1px solid rgba(60, 0, 0, 0.20);
  }

  tr.qualified { border-left: 2px solid rgba(220, 0, 0, 0.70); td { color: #fff; } }
  tr:last-child td { border-bottom: none; }
}

.rank { font-family: t.$font-display; color: rgba(180, 50, 50, 0.55); font-size: 12px; }
.player-name { font-weight: 700; letter-spacing: 1px; text-transform: uppercase; }
.fighter-name { font-size: 11px; color: rgba(160, 130, 130, 0.70); }
.pts { font-family: t.$font-display; font-size: 16px; color: rgba(220, 80, 80, 0.80); }
.badge {
  font-family: t.$font-display; font-size: 8px; letter-spacing: 2px;
  color: rgba(220, 80, 80, 0.90);
  border: 1px solid rgba(180, 0, 0, 0.50); padding: 2px 6px; white-space: nowrap;
}
```

- [ ] **Étape 2 : Vérifier**

```bash
cd frontend && npx ng build --watch=false 2>&1 | tail -5
```

---

## Task 14 — GroupStageComponent

Grille de GroupCards.

**Files:**
- Create: `frontend/src/app/bracket/group-stage/group-stage.component.ts|html|scss`

- [ ] **Étape 1 : Implémenter**

```typescript
// frontend/src/app/bracket/group-stage/group-stage.component.ts
import { Component, input } from '@angular/core';
import { GroupCardComponent } from '../group-card/group-card.component';
import { TournamentGroup } from '../../models/bracket.models';

@Component({
  selector: 'app-group-stage',
  imports: [GroupCardComponent],
  templateUrl: './group-stage.component.html',
  styleUrl: './group-stage.component.scss'
})
export class GroupStageComponent {
  groups = input.required<TournamentGroup[]>();
}
```

```html
<!-- frontend/src/app/bracket/group-stage/group-stage.component.html -->
<div class="groups-grid">
  @for (group of groups(); track group.groupId) {
    <app-group-card [group]="group" />
  }
</div>
```

```scss
// frontend/src/app/bracket/group-stage/group-stage.component.scss
.groups-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 24px;
}
```

- [ ] **Étape 2 : Vérifier**

```bash
cd frontend && npx ng build --watch=false 2>&1 | tail -5
```

---

## Task 15 — TournamentBracketPageComponent (assemblage final)

**Files:**
- Create: `frontend/src/app/bracket/bracket-page/bracket-page.component.ts|html|scss`

- [ ] **Étape 1 : Implémenter**

```typescript
// frontend/src/app/bracket/bracket-page/bracket-page.component.ts
import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TournamentBracketService } from '../../services/tournament-bracket.service';
import { TournamentBracketData } from '../../models/bracket.models';
import { BracketTitleComponent } from '../bracket-title/bracket-title.component';
import { StageTabsComponent, StageTab } from '../stage-tabs/stage-tabs.component';
import { GroupStageComponent } from '../group-stage/group-stage.component';
import { WinnersBracketComponent } from '../winners-bracket/winners-bracket.component';
import { LosersBracketComponent } from '../losers-bracket/losers-bracket.component';
import { GrandFinalComponent } from '../grand-final/grand-final.component';
import { ChampionCardComponent } from '../champion-card/champion-card.component';

@Component({
  selector: 'app-bracket-page',
  imports: [
    BracketTitleComponent, StageTabsComponent,
    GroupStageComponent, WinnersBracketComponent,
    LosersBracketComponent, GrandFinalComponent, ChampionCardComponent
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

  onTabChange(tab: StageTab): void {
    this.activeTab.set(tab);
  }
}
```

```html
<!-- frontend/src/app/bracket/bracket-page/bracket-page.component.html -->
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
        <app-winners-bracket [rounds]="d.winnersBracket" />
        @if (d.losersBracket.length > 0) {
          <app-losers-bracket [rounds]="d.losersBracket" />
        }
        <app-grand-final [grandFinal]="d.grandFinal" [bracketReset]="d.bracketReset" />
        @if (d.champion) {
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

```scss
// frontend/src/app/bracket/bracket-page/bracket-page.component.scss
.page {
  position: relative; z-index: 1;
  max-width: 1400px; margin: 0 auto;
  padding: 48px 24px 80px;
}

.bracket-view {
  display: flex; flex-direction: column;
  align-items: flex-start; gap: 0;
  overflow-x: auto;
}

.state-msg {
  font-family: 'Bebas Neue', sans-serif; font-size: 18px; letter-spacing: 4px;
  text-align: center; margin-top: 120px; color: rgba(220, 80, 80, 0.70);
  &.error { color: rgba(255, 60, 60, 0.80); }
}
```

- [ ] **Étape 2 : Build final**

```bash
cd frontend && npx ng build --watch=false 2>&1 | tail -5
```
Attendu : `Application bundle generation complete.`

- [ ] **Étape 3 : Tous les tests au vert**

```bash
cd frontend && npx ng test --watch=false 2>&1 | grep -E 'Executed|FAILED|SUCCESS'
```
Attendu : `Executed N of N SUCCESS`

- [ ] **Étape 4 : Vérifier visuellement**

```bash
cd frontend && npx ng serve
```

Ouvrir `http://localhost:4200/tournament/1/bracket`.

Vérifier :
- Fond Tekken 8 + overlay obsidien visible
- Message "Impossible de charger le bracket." s'affiche (backend absent — comportement attendu)
- Pas d'erreurs Angular dans la console

---

## Prochaine étape

Créer un plan séparé pour le endpoint Java/Jersey `GET /api/tournaments/{id}/bracket` qui retourne un JSON conforme à `TournamentBracketData` défini dans la Task 2.
