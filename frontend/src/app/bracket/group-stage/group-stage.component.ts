import { Component, input } from '@angular/core';
import { GroupCardComponent } from '../group-card/group-card.component';
import { TournamentGroup } from '../../models/bracket.models';

@Component({
  selector: 'app-group-stage',
  imports: [GroupCardComponent],
  templateUrl: './group-stage.component.html',
  styleUrl: './group-stage.component.scss'
})
export class GroupStageComponent {
  groups = input.required<TournamentGroup[]>();
}
