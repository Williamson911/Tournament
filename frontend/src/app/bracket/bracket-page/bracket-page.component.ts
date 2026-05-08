import { AfterViewInit, Component, ElementRef, HostListener, inject, signal, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TournamentBracketService } from '../../services/tournament-bracket.service';
import { BattleEffectsService } from '../../services/battle-effects.service';
import { TournamentBracketData, BracketMatch } from '../../models/bracket.models';
import { BracketTitleComponent } from '../bracket-title/bracket-title.component';
import { StageTabsComponent, StageTab } from '../stage-tabs/stage-tabs.component';
import { GroupStageComponent } from '../group-stage/group-stage.component';
import { BracketLayoutComponent } from '../bracket-layout/bracket-layout.component';
import { BracketConnectorComponent } from '../bracket-connector/bracket-connector.component';
import { GrandFinalComponent } from '../grand-final/grand-final.component';
import { ChampionCardComponent } from '../champion-card/champion-card.component';
import { GrandFinalPopupComponent } from '../grand-final-popup/grand-final-popup.component';

@Component({
  selector: 'app-bracket-page',
  imports: [
    BracketTitleComponent, StageTabsComponent, GroupStageComponent,
    BracketLayoutComponent, BracketConnectorComponent,
    GrandFinalComponent, ChampionCardComponent, GrandFinalPopupComponent
  ],
  templateUrl: './bracket-page.component.html',
  styleUrl: './bracket-page.component.scss'
})
export class BracketPageComponent implements OnInit, AfterViewInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private service = inject(TournamentBracketService);
  private battleEffects = inject(BattleEffectsService);

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
  fighting = signal(false);
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

  @HostListener('window:keydown', ['$event'])
  onShortcutKey(event: KeyboardEvent): void {
    if (event.ctrlKey || event.metaKey || event.altKey) return;
    const target = event.target as HTMLElement | null;
    if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable)) return;
    const d = this.data();
    if (!d || d.status !== 'IN_PROGRESS' || d.champion || this.simulating()) return;
    const key = event.key.toLowerCase();
    if (key === 'f') { event.preventDefault(); this.simulateRound(); }
    else if (key === 'c') { event.preventDefault(); this.simulateMatch(); }
  }

  simulateRound(): void {
    const before = this.snapshotMatches(this.data());
    this.simulating.set(true);
    this.fighting.set(true);
    setTimeout(() => this.fighting.set(false), 600);
    this.service.simulateNextRound(this.tournamentId).subscribe({
      next: d => {
        const { resolvedIds, arrivingKeys } = this.diffMatches(before, d);
        this.data.set(d);
        this.battleEffects.trigger(resolvedIds, arrivingKeys);
        this.simulating.set(false);
        setTimeout(() => { this.measureGfConnector(); this.wireStickyScroll(); });
      },
      error: () => this.simulating.set(false)
    });
  }

  simulateMatch(): void {
    const before = this.snapshotMatches(this.data());
    this.simulating.set(true);
    this.fighting.set(true);
    setTimeout(() => this.fighting.set(false), 600);
    this.service.simulateOneMatch(this.tournamentId).subscribe({
      next: d => {
        const { resolvedIds, arrivingKeys } = this.diffMatches(before, d);
        this.data.set(d);
        this.battleEffects.trigger(resolvedIds, arrivingKeys);
        this.simulating.set(false);
        setTimeout(() => { this.measureGfConnector(); this.wireStickyScroll(); });
      },
      error: () => this.simulating.set(false)
    });
  }

  private snapshotMatches(d: TournamentBracketData | null): Map<number, { complete: boolean; p1: boolean; p2: boolean }> {
    const map = new Map<number, { complete: boolean; p1: boolean; p2: boolean }>();
    if (!d) return map;
    const all: BracketMatch[] = [];
    d.winnersBracket.forEach(r => all.push(...r.matches));
    d.losersBracket.forEach(r => all.push(...r.matches));
    if (d.grandFinal) all.push(d.grandFinal);
    all.forEach(m => map.set(m.matchId, {
      complete: m.isComplete,
      p1: m.participant1 !== null,
      p2: m.participant2 !== null,
    }));
    return map;
  }

  private diffMatches(
    before: Map<number, { complete: boolean; p1: boolean; p2: boolean }>,
    after: TournamentBracketData,
  ): { resolvedIds: number[]; arrivingKeys: string[] } {
    const resolvedIds: number[] = [];
    const arrivingKeys: string[] = [];
    const all: BracketMatch[] = [];
    after.winnersBracket.forEach(r => all.push(...r.matches));
    after.losersBracket.forEach(r => all.push(...r.matches));
    if (after.grandFinal) all.push(after.grandFinal);
    all.forEach(m => {
      const prev = before.get(m.matchId);
      if (m.isComplete && (!prev || !prev.complete)) resolvedIds.push(m.matchId);
      if (m.participant1 !== null && (!prev || !prev.p1)) arrivingKeys.push(`${m.matchId}-1`);
      if (m.participant2 !== null && (!prev || !prev.p2)) arrivingKeys.push(`${m.matchId}-2`);
    });
    return { resolvedIds, arrivingKeys };
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
