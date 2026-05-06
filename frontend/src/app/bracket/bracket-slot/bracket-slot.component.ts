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
  matchComplete = input<boolean>(false);
}
