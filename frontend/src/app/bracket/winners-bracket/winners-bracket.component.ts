import { Component, input } from '@angular/core';
import { BracketRoundComponent } from '../bracket-round/bracket-round.component';
import { BracketRound } from '../../models/bracket.models';

@Component({
  selector: 'app-winners-bracket',
  imports: [BracketRoundComponent],
  templateUrl: './winners-bracket.component.html',
  styleUrl: './winners-bracket.component.scss'
})
export class WinnersBracketComponent {
  rounds = input.required<BracketRound[]>();
}
