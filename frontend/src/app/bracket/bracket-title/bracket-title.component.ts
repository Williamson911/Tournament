import { Component, input } from '@angular/core';

@Component({
  selector: 'app-bracket-title',
  templateUrl: './bracket-title.component.html',
  styleUrl: './bracket-title.component.scss'
})
export class BracketTitleComponent {
  title = input.required<string>();
  subtitle = input<string>('');
}
