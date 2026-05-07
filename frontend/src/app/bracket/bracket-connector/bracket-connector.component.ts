import { Component, computed, input } from '@angular/core';

interface Pair { topY: number; botY: number; mid: number; }

@Component({
  selector: 'app-bracket-connector',
  imports: [],
  template: `
    <svg [attr.width]="WIDTH" [attr.height]="svgHeight()"
         [attr.viewBox]="'0 0 ' + WIDTH + ' ' + svgHeight()" fill="none">
      @for (pair of pairs(); track $index) {
        <path [attr.d]="'M2,' + pair.topY + ' H26 V' + pair.mid + ' H' + WIDTH"
              stroke="rgba(180,0,0,.42)" stroke-width="1.5"/>
        <path [attr.d]="'M2,' + pair.botY + ' H26 V' + pair.mid"
              stroke="rgba(180,0,0,.42)" stroke-width="1.5"/>
        <circle [attr.cx]="WIDTH" [attr.cy]="pair.mid" r="3.5" fill="rgba(230,0,0,.75)"/>
        <circle [attr.cx]="2" [attr.cy]="pair.topY" r="2.5" fill="rgba(180,0,0,.55)"/>
        <circle [attr.cx]="2" [attr.cy]="pair.botY" r="2.5" fill="rgba(180,0,0,.55)"/>
      }
    </svg>
  `,
  styleUrl: './bracket-connector.component.scss'
})
export class BracketConnectorComponent {
  /** Number of matches in the preceding round (must be even). */
  matchCount = input.required<number>();

  protected readonly WIDTH = 52;
  private readonly SLOT_H = 62;
  private readonly VS_H = 14;
  private readonly MATCH_H = this.SLOT_H * 2 + this.VS_H;  // 138
  private readonly GAP = 24;
  private readonly PAIR_H = this.MATCH_H * 2 + this.GAP;   // 300

  protected pairs = computed<Pair[]>(() => {
    const pairCount = Math.ceil(this.matchCount() / 2);
    return Array.from({ length: pairCount }, (_, i) => {
      const offset = i * (this.PAIR_H + this.GAP);
      const topY = offset + this.MATCH_H / 2;
      const botY = offset + this.MATCH_H + this.GAP + this.MATCH_H / 2;
      return { topY, botY, mid: (topY + botY) / 2 };
    });
  });

  protected svgHeight = computed(() => {
    const p = this.pairs();
    if (!p.length) return 0;
    return p[p.length - 1].botY + this.MATCH_H / 2;
  });
}
