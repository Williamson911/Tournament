import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { TournamentBracketService } from '../../services/tournament-bracket.service';
import { TournamentBracketData } from '../../models/bracket.models';
import { BracketTitleComponent } from '../bracket-title/bracket-title.component';
import { StageTabsComponent, StageTab } from '../stage-tabs/stage-tabs.component';
import { GroupStageComponent } from '../group-stage/group-stage.component';
import { WinnersBracketComponent } from '../winners-bracket/winners-bracket.component';
import { LosersBracketComponent } from '../losers-bracket/losers-bracket.component';
import { GrandFinalComponent } from '../grand-final/grand-final.component';
import { ChampionCardComponent } from '../champion-card/champion-card.component';

const JIN_IMG = 'https://static.wikia.nocookie.net/tekken/images/a/ab/T8_-_Jin_Render_%28High_Resolution%29.jpg/revision/latest/scale-to-width-down/368?cb=20251202234836&path-prefix=en';
const KAZUYA_IMG = 'https://static.wikia.nocookie.net/tekken/images/b/bf/Kazuya_Mishima_TK8_%28High_Resolution%29.jpg/revision/latest/scale-to-width-down/291?cb=20251202235851&path-prefix=en';
const KING_IMG = 'https://static.wikia.nocookie.net/tekken/images/7/77/Tk2_king.png/revision/latest/scale-to-width-down/267?cb=20200823061154&path-prefix=en';
const NINA_IMG = 'https://static.wikia.nocookie.net/tekken/images/c/c8/Tekken_8_-_Nina_Williams_Official_Render.jpg/revision/latest/scale-to-width-down/350?cb=20230901101613&path-prefix=en';
const PAUL_IMG = 'https://static.wikia.nocookie.net/tekken/images/4/4e/Paul_Phoenix_TK8_%28High_Resolution%29.jpg/revision/latest/scale-to-width-down/325?cb=20251203014116&path-prefix=en';
const LAW_IMG = 'https://static.wikia.nocookie.net/tekken/images/d/d2/Marshall_Law_TK8_%28High_Resolution%29.jpeg/revision/latest/scale-to-width-down/392?cb=20251203013002&path-prefix=en';
const HWOARANG_IMG = 'https://static.wikia.nocookie.net/tekken/images/1/19/Hwoarang_TK8_render.jpg/revision/latest/scale-to-width-down/333?cb=20230516120648&path-prefix=en';
const LILI_IMG = 'https://static.wikia.nocookie.net/tekken/images/6/60/Tekken_8_-_Lili_Render.jpeg/revision/latest/scale-to-width-down/375?cb=20251203013354&path-prefix=en';

const MOCK_DATA: TournamentBracketData = {
  tournamentId: 1,
  tournamentName: 'Tekken 8 Championship',
  hasGroupStage: false,
  groups: [],
  winnersBracket: [
    {
      roundId: 1, label: 'WB QUARTS DE FINALE',
      matches: [
        {
          matchId: 1,
          participant1: { playerId: 1, playerName: 'MaxCombo', fighterName: 'Jin Kazama', fighterImageUrl: JIN_IMG, score: 2, isWinner: true, isEliminated: false },
          participant2: { playerId: 5, playerName: 'LawBreaker', fighterName: 'Marshall Law', fighterImageUrl: LAW_IMG, score: 0, isWinner: false, isEliminated: false },
          isComplete: true, isBracketReset: false
        },
        {
          matchId: 2,
          participant1: { playerId: 2, playerName: 'DevilFist', fighterName: 'Kazuya', fighterImageUrl: KAZUYA_IMG, score: 2, isWinner: true, isEliminated: false },
          participant2: { playerId: 6, playerName: 'PaulDynamo', fighterName: 'Paul Phoenix', fighterImageUrl: PAUL_IMG, score: 1, isWinner: false, isEliminated: false },
          isComplete: true, isBracketReset: false
        },
        {
          matchId: 3,
          participant1: { playerId: 3, playerName: 'JaggedMask', fighterName: 'King', fighterImageUrl: KING_IMG, score: 2, isWinner: true, isEliminated: false },
          participant2: { playerId: 7, playerName: 'FlameKicker', fighterName: 'Hwoarang', fighterImageUrl: HWOARANG_IMG, score: 1, isWinner: false, isEliminated: false },
          isComplete: true, isBracketReset: false
        },
        {
          matchId: 4,
          participant1: { playerId: 4, playerName: 'Silencer', fighterName: 'Nina Williams', fighterImageUrl: NINA_IMG, score: 2, isWinner: true, isEliminated: false },
          participant2: { playerId: 8, playerName: 'LiliRose', fighterName: 'Lili', fighterImageUrl: LILI_IMG, score: 0, isWinner: false, isEliminated: false },
          isComplete: true, isBracketReset: false
        },
      ]
    },
    {
      roundId: 2, label: 'WB DEMI-FINALES',
      matches: [
        {
          matchId: 5,
          participant1: { playerId: 1, playerName: 'MaxCombo', fighterName: 'Jin Kazama', fighterImageUrl: JIN_IMG, score: 2, isWinner: true, isEliminated: false },
          participant2: { playerId: 2, playerName: 'DevilFist', fighterName: 'Kazuya', fighterImageUrl: KAZUYA_IMG, score: 1, isWinner: false, isEliminated: false },
          isComplete: true, isBracketReset: false
        },
        {
          matchId: 6,
          participant1: { playerId: 3, playerName: 'JaggedMask', fighterName: 'King', fighterImageUrl: KING_IMG, score: 2, isWinner: true, isEliminated: false },
          participant2: { playerId: 4, playerName: 'Silencer', fighterName: 'Nina Williams', fighterImageUrl: NINA_IMG, score: 0, isWinner: false, isEliminated: false },
          isComplete: true, isBracketReset: false
        },
      ]
    },
  ],
  losersBracket: [
    {
      roundId: 1, label: 'LB ROUND 1',
      matches: [
        {
          matchId: 7,
          participant1: { playerId: 5, playerName: 'LawBreaker', fighterName: 'Marshall Law', fighterImageUrl: LAW_IMG, score: 2, isWinner: true, isEliminated: false },
          participant2: { playerId: 8, playerName: 'LiliRose', fighterName: 'Lili', fighterImageUrl: LILI_IMG, score: 0, isWinner: false, isEliminated: true },
          isComplete: true, isBracketReset: false
        },
        {
          matchId: 8,
          participant1: { playerId: 6, playerName: 'PaulDynamo', fighterName: 'Paul Phoenix', fighterImageUrl: PAUL_IMG, score: 2, isWinner: true, isEliminated: false },
          participant2: { playerId: 7, playerName: 'FlameKicker', fighterName: 'Hwoarang', fighterImageUrl: HWOARANG_IMG, score: 1, isWinner: false, isEliminated: true },
          isComplete: true, isBracketReset: false
        },
      ]
    },
    {
      roundId: 2, label: 'LB DEMI-FINALE',
      matches: [
        {
          matchId: 9,
          participant1: { playerId: 2, playerName: 'DevilFist', fighterName: 'Kazuya', fighterImageUrl: KAZUYA_IMG, score: 2, isWinner: true, isEliminated: false },
          participant2: { playerId: 5, playerName: 'LawBreaker', fighterName: 'Marshall Law', fighterImageUrl: LAW_IMG, score: 1, isWinner: false, isEliminated: true },
          isComplete: true, isBracketReset: false
        },
        {
          matchId: 10,
          participant1: { playerId: 4, playerName: 'Silencer', fighterName: 'Nina Williams', fighterImageUrl: NINA_IMG, score: 2, isWinner: true, isEliminated: false },
          participant2: { playerId: 6, playerName: 'PaulDynamo', fighterName: 'Paul Phoenix', fighterImageUrl: PAUL_IMG, score: 0, isWinner: false, isEliminated: true },
          isComplete: true, isBracketReset: false
        },
      ]
    },
    {
      roundId: 3, label: 'LB FINALE',
      matches: [
        {
          matchId: 11,
          participant1: { playerId: 2, playerName: 'DevilFist', fighterName: 'Kazuya', fighterImageUrl: KAZUYA_IMG, score: 2, isWinner: true, isEliminated: false },
          participant2: { playerId: 4, playerName: 'Silencer', fighterName: 'Nina Williams', fighterImageUrl: NINA_IMG, score: 1, isWinner: false, isEliminated: true },
          isComplete: true, isBracketReset: false
        },
      ]
    },
  ],
  grandFinal: {
    matchId: 12,
    participant1: { playerId: 1, playerName: 'MaxCombo', fighterName: 'Jin Kazama', fighterImageUrl: JIN_IMG, score: 3, isWinner: true, isEliminated: false },
    participant2: { playerId: 2, playerName: 'DevilFist', fighterName: 'Kazuya', fighterImageUrl: KAZUYA_IMG, score: 2, isWinner: false, isEliminated: true },
    isComplete: true, isBracketReset: false
  },
  bracketReset: null,
  champion: { playerId: 1, playerName: 'MaxCombo', fighterName: 'Jin Kazama', fighterImageUrl: JIN_IMG, score: 3, isWinner: true, isEliminated: false }
};

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
        // TODO: retirer le fallback mock une fois le backend prêt
        this.data.set(MOCK_DATA);
        this.loading.set(false);
      }
    });
  }

  onTabChange(tab: StageTab): void {
    this.activeTab.set(tab);
  }
}
