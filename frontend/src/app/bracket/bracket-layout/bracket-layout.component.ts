import { Component, input } from '@angular/core';
import { WinnersBracketComponent } from '../winners-bracket/winners-bracket.component';
import { LosersBracketComponent } from '../losers-bracket/losers-bracket.component';
import { BracketRound } from '../../models/bracket.models';

@Component({
  selector: 'app-bracket-layout',
  imports: [WinnersBracketComponent, LosersBracketComponent],
  templateUrl: './bracket-layout.component.html',
  styleUrl: './bracket-layout.component.scss'
})
export class BracketLayoutComponent {
  winnersBracket = input.required<BracketRound[]>();
  losersBracket = input.required<BracketRound[]>();
}
