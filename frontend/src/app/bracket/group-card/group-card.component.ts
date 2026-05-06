import { Component, input } from '@angular/core';
import { TournamentGroup } from '../../models/bracket.models';

@Component({
  selector: 'app-group-card',
  templateUrl: './group-card.component.html',
  styleUrl: './group-card.component.scss'
})
export class GroupCardComponent {
  group = input.required<TournamentGroup>();
}
