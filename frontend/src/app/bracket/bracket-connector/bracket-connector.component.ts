import { Component, computed, input } from '@angular/core';

interface Pair { topY: number; botY: number; mid: number; }

@Component({
  selector: 'app-bracket-connector',
  imports: [],
  host: { '[class.stretch]': 'isStretch()' },
  template: `
    @if (isStretch()) {
      <svg [attr.width]="WIDTH" [attr.height]="svgH()" fill="none" overflow="visible">
        @let p = stretchPair();
        @if (p.topY === p.botY) {
          <path [attr.d]="'M2,' + p.mid + ' H' + WIDTH"
                stroke="rgba(180,0,0,.42)" stroke-width="1.5"/>
          <circle [attr.cx]="2" [attr.cy]="p.mid" r="2.5" fill="rgba(180,0,0,.55)"/>
          <circle [attr.cx]="WIDTH" [attr.cy]="p.mid" r="3.5" fill="rgba(230,0,0,.75)"/>
        } @else {
          <path [attr.d]="'M2,' + p.topY + ' H26 V' + p.mid + ' H' + WIDTH"
                stroke="rgba(180,0,0,.42)" stroke-width="1.5"/>
          <path [attr.d]="'M2,' + p.botY + ' H26 V' + p.mid"
                stroke="rgba(180,0,0,.42)" stroke-width="1.5"/>
          <circle [attr.cx]="WIDTH" [attr.cy]="p.mid" r="3.5" fill="rgba(230,0,0,.75)"/>
          <circle [attr.cx]="2" [attr.cy]="p.topY" r="2.5" fill="rgba(180,0,0,.55)"/>
          <circle [attr.cx]="2" [attr.cy]="p.botY" r="2.5" fill="rgba(180,0,0,.55)"/>
        }
      </svg>
    } @else {
      <svg [attr.width]="WIDTH" [attr.height]="geomHeight()"
           [attr.viewBox]="'0 0 ' + WIDTH + ' ' + geomHeight()" fill="none" overflow="visible">
        @for (pair of pairs(); track $index) {
          @if (pair.topY === pair.botY) {
            <path [attr.d]="'M2,' + pair.mid + ' H' + WIDTH"
                  stroke="rgba(180,0,0,.42)" stroke-width="1.5"/>
            <circle [attr.cx]="2" [attr.cy]="pair.mid" r="2.5" fill="rgba(180,0,0,.55)"/>
            <circle [attr.cx]="WIDTH" [attr.cy]="pair.mid" r="3.5" fill="rgba(230,0,0,.75)"/>
          } @else {
            <path [attr.d]="'M2,' + pair.topY + ' H26 V' + pair.mid + ' H' + WIDTH"
                  stroke="rgba(180,0,0,.42)" stroke-width="1.5"/>
            <path [attr.d]="'M2,' + pair.botY + ' H26 V' + pair.mid"
                  stroke="rgba(180,0,0,.42)" stroke-width="1.5"/>
            <circle [attr.cx]="WIDTH" [attr.cy]="pair.mid" r="3.5" fill="rgba(230,0,0,.75)"/>
            <circle [attr.cx]="2" [attr.cy]="pair.topY" r="2.5" fill="rgba(180,0,0,.55)"/>
            <circle [attr.cx]="2" [attr.cy]="pair.botY" r="2.5" fill="rgba(180,0,0,.55)"/>
          }
        }
      </svg>
    }
  `,
  styleUrl: './bracket-connector.component.scss'
})
export class BracketConnectorComponent {
  matchCount = input<number>(1);
  /** Absolute pixel Y (relative to this SVG's top) of the WB Final center. */
  topLineY = input<number | null>(null);
  /** Absolute pixel Y (relative to this SVG's top) of the LB Final center. */
  bottomLineY = input<number | null>(null);
  /** Total pixel height of the SVG when in stretch mode. */
  svgH = input<number | null>(null);

  protected readonly WIDTH = 52;
  private readonly SLOT_H = 62;
  private readonly VS_H = 14;
  private readonly MATCH_H = this.SLOT_H * 2 + this.VS_H;
  private readonly GAP = 24;
  private readonly PAIR_H = this.MATCH_H * 2 + this.GAP;

  protected isStretch = computed(() =>
    this.topLineY() !== null && this.bottomLineY() !== null && this.svgH() !== null
  );

  protected stretchPair = computed<Pair>(() => {
    const topY = this.topLineY()!;
    const botY = this.bottomLineY()!;
    return { topY, botY, mid: (topY + botY) / 2 };
  });

  protected pairs = computed<Pair[]>(() => {
    const count = this.matchCount();
    if (count <= 1) {
      const mid = this.MATCH_H / 2;
      return [{ topY: mid, botY: mid, mid }];
    }
    const pairCount = Math.ceil(count / 2);
    return Array.from({ length: pairCount }, (_, i) => {
      const offset = i * (this.PAIR_H + this.GAP);
      const topY = offset + this.MATCH_H / 2;
      const botY = offset + this.MATCH_H + this.GAP + this.MATCH_H / 2;
      return { topY, botY, mid: (topY + botY) / 2 };
    });
  });

  protected geomHeight = computed(() => {
    const p = this.pairs();
    if (!p.length) return 0;
    const last = p[p.length - 1];
    return last.topY === last.botY ? last.mid + this.MATCH_H / 2 : last.botY + this.MATCH_H / 2;
  });
}
