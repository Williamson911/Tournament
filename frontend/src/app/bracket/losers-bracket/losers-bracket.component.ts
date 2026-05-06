import { Component, input } from '@angular/core';
import { BracketRoundComponent } from '../bracket-round/bracket-round.component';
import { BracketRound } from '../../models/bracket.models';

@Component({
  selector: 'app-losers-bracket',
  imports: [BracketRoundComponent],
  templateUrl: './losers-bracket.component.html',
  styleUrl: './losers-bracket.component.scss'
})
export class LosersBracketComponent {
  rounds = input.required<BracketRound[]>();
}
