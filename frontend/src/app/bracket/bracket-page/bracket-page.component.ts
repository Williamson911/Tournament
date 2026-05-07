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
  gfSvgH = signal<number | null>(null);
  gfTopLineY = signal<number | null>(null);
  gfBottomLineY = signal<number | null>(null);
  gfMarginTop = signal<number | null>(null);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.service.getBracket(id).subscribe({
      next: d => {
        this.data.set(d);
        this.loading.set(false);
        if (d.hasGroupStage) this.activeTab.set('groups');
        setTimeout(() => this.measureGfConnector());
      },
      error: () => {
        this.error.set('Impossible de charger le bracket.');
        this.loading.set(false);
      }
    });
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.measureGfConnector());
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

    // Use the last app-bracket-match within each round to hit the VS-level center
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

    // Align GF so its VS separator sits exactly at the convergence midpoint
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
