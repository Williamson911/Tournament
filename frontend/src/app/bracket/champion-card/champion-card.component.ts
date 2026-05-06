import { Component, input } from '@angular/core';
import { MatchParticipant } from '../../models/bracket.models';

@Component({
  selector: 'app-champion-card',
  templateUrl: './champion-card.component.html',
  styleUrl: './champion-card.component.scss'
})
export class ChampionCardComponent {
  champion = input.required<MatchParticipant>();
}
