import { Component, input } from '@angular/core';
import { BracketSlotComponent } from '../bracket-slot/bracket-slot.component';
import { BracketMatch } from '../../models/bracket.models';

@Component({
  selector: 'app-bracket-match',
  imports: [BracketSlotComponent],
  templateUrl: './bracket-match.component.html',
  styleUrl: './bracket-match.component.scss'
})
export class BracketMatchComponent {
  match = input.required<BracketMatch>();
}
