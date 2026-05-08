import { AfterViewInit, Component, ElementRef, inject, signal, OnDestroy, OnInit, ViewChild } from '@angular/core';
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
export class BracketPageComponent implements OnInit, AfterViewInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private service = inject(TournamentBracketService);

  @ViewChild('bracketLayoutRef', { read: ElementRef }) private bracketLayoutRef?: ElementRef;
  @ViewChild('grandFinalRef', { read: ElementRef }) private grandFinalRef?: ElementRef;
  @ViewChild('bracketClipperRef', { read: ElementRef }) private bracketClipperRef?: ElementRef;
  @ViewChild('bracketViewRef', { read: ElementRef }) private bracketViewRef?: ElementRef;
  @ViewChild('stickyScrollRef', { read: ElementRef }) private stickyScrollRef?: ElementRef;

  data = signal<TournamentBracketData | null>(null);
  activeTab = signal<StageTab>('bracket');
  loading = signal(true);
  error = signal<string | null>(null);
  launching = signal(false);
  generating = signal(false);
  simulating = signal(false);
  resetting = signal(false);
  gfSvgH = signal<number | null>(null);
  gfTopLineY = signal<number | null>(null);
  gfBottomLineY = signal<number | null>(null);
  gfMarginTop = signal<number | null>(null);
  scrollX = signal(0);
  contentWidth = signal(0);

  private tournamentId = 0;
  private stickyScrollWired = false;
  private resizeObserver?: ResizeObserver;

  ngOnInit(): void {
    this.tournamentId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadBracket();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.measureGfConnector();
      this.wireStickyScroll();
    });
  }

  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
  }

  private wireStickyScroll(): void {
    const view = this.bracketViewRef?.nativeElement as HTMLElement | undefined;
    const sticky = this.stickyScrollRef?.nativeElement as HTMLElement | undefined;
    const clipper = this.bracketClipperRef?.nativeElement as HTMLElement | undefined;
    if (!view || !sticky || !clipper) return;

    const updateWidths = () => this.contentWidth.set(view.scrollWidth);
    updateWidths();

    if (!this.resizeObserver) {
      this.resizeObserver = new ResizeObserver(updateWidths);
      this.resizeObserver.observe(view);
      this.resizeObserver.observe(clipper);
    }

    if (this.stickyScrollWired) return;
    this.stickyScrollWired = true;

    sticky.addEventListener('scroll', () => {
      const max = Math.max(0, view.scrollWidth - clipper.clientWidth);
      this.scrollX.set(Math.min(sticky.scrollLeft, max));
    });

    clipper.addEventListener('wheel', (e: WheelEvent) => {
      const dx = e.shiftKey ? e.deltaY : e.deltaX;
      if (!dx) return;
      const max = Math.max(0, view.scrollWidth - clipper.clientWidth);
      if (max <= 0) return;
      e.preventDefault();
      sticky.scrollLeft = Math.max(0, Math.min(max, sticky.scrollLeft + dx));
    }, { passive: false });
  }

  launchGroupStage(): void {
    this.launching.set(true);
    this.service.launchGroupStage(this.tournamentId).subscribe({
      next: () => this.loadBracket(),
      error: () => this.launching.set(false)
    });
  }

  generateBracket(): void {
    this.generating.set(true);
    this.service.generateBracket(this.tournamentId).subscribe({
      next: () => this.loadBracket(),
      error: () => this.generating.set(false)
    });
  }

  simulateRound(): void {
    this.simulating.set(true);
    this.service.simulateNextRound(this.tournamentId).subscribe({
      next: d => {
        this.data.set(d);
        this.simulating.set(false);
        setTimeout(() => { this.measureGfConnector(); this.wireStickyScroll(); });
      },
      error: () => this.simulating.set(false)
    });
  }

  resetTournament(): void {
    if (!confirm('Réinitialiser le tournoi ? Les matchs seront effacés et le tournoi reviendra en DRAFT.')) return;
    this.resetting.set(true);
    this.service.resetTournament(this.tournamentId).subscribe({
      next: () => this.loadBracket(),
      error: () => this.resetting.set(false)
    });
  }

  private loadBracket(): void {
    this.service.getBracket(this.tournamentId).subscribe({
      next: d => {
        this.data.set(d);
        this.loading.set(false);
        this.launching.set(false);
        this.generating.set(false);
        this.simulating.set(false);
        this.resetting.set(false);
        setTimeout(() => { this.measureGfConnector(); this.wireStickyScroll(); });
      },
      error: () => {
        this.error.set('Impossible de charger le bracket.');
        this.loading.set(false);
        this.launching.set(false);
        this.generating.set(false);
        this.simulating.set(false);
        this.resetting.set(false);
      }
    });
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
