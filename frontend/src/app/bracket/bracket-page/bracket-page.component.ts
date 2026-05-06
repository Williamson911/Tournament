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
