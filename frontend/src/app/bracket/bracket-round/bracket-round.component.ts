import { Component, input } from '@angular/core';
import { BracketMatchComponent } from '../bracket-match/bracket-match.component';
import { BracketRound } from '../../models/bracket.models';

@Component({
  selector: 'app-bracket-round',
  imports: [BracketMatchComponent],
  templateUrl: './bracket-round.component.html',
  styleUrl: './bracket-round.component.scss'
})
export class BracketRoundComponent {
  round = input.required<BracketRound>();
}
