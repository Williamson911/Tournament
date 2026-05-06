import { Component, input } from '@angular/core';
import { BracketMatchComponent } from '../bracket-match/bracket-match.component';
import { BracketMatch } from '../../models/bracket.models';

@Component({
  selector: 'app-grand-final',
  imports: [BracketMatchComponent],
  templateUrl: './grand-final.component.html',
  styleUrl: './grand-final.component.scss'
})
export class GrandFinalComponent {
  grandFinal = input.required<BracketMatch>();
  bracketReset = input<BracketMatch | null>(null);
}
