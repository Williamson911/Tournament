import { Component, computed, inject, input } from '@angular/core';
import { MatchParticipant } from '../../models/bracket.models';
import { BattleEffectsService } from '../../services/battle-effects.service';

@Component({
  selector: 'app-bracket-slot',
  templateUrl: './bracket-slot.component.html',
  styleUrl: './bracket-slot.component.scss'
})
export class BracketSlotComponent {
  participant = input<MatchParticipant | null>(null);
  seed = input<number | string>('');
  matchComplete = input<boolean>(false);
  promoted = input<boolean>(true);
  matchId = input<number | null>(null);
  slotIndex = input<1 | 2 | null>(null);

  private effects = inject(BattleEffectsService);

  justResolved = computed(() => {
    const id = this.matchId();
    return id !== null && this.effects.resolvedMatchIds().has(id);
  });

  justArrived = computed(() => {
    const id = this.matchId();
    const idx = this.slotIndex();
    if (id === null || idx === null) return false;
    return this.effects.arrivingSlots().has(`${id}-${idx}`);
  });
}
