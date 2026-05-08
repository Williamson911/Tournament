import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class BattleEffectsService {
  resolvedMatchIds = signal<Set<number>>(new Set());
  arrivingSlots = signal<Set<string>>(new Set());

  trigger(matchIds: number[], arrivingKeys: string[], durationMs = 1400): void {
    this.resolvedMatchIds.set(new Set(matchIds));
    this.arrivingSlots.set(new Set(arrivingKeys));
    setTimeout(() => {
      this.resolvedMatchIds.set(new Set());
      this.arrivingSlots.set(new Set());
    }, durationMs);
  }

  static slotKey(matchId: number, slotIndex: 1 | 2): string {
    return `${matchId}-${slotIndex}`;
  }
}
