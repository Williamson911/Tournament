import { Component, input, output } from '@angular/core';

export type StageTab = 'groups' | 'bracket';

@Component({
  selector: 'app-stage-tabs',
  templateUrl: './stage-tabs.component.html',
  styleUrl: './stage-tabs.component.scss'
})
export class StageTabsComponent {
  activeTab = input.required<StageTab>();
  tabChange = output<StageTab>();
}
