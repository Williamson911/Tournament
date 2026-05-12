import { Component, input, signal } from '@angular/core';
import { MatchParticipant } from '../../models/bracket.models';

@Component({
  selector: 'app-champion-card',
  templateUrl: './champion-card.component.html',
  styleUrl: './champion-card.component.scss'
})
export class ChampionCardComponent {
  champion = input.required<MatchParticipant>();
  closed = signal(false);

  close(): void { this.closed.set(true); }
}
