import { Component, input, signal } from '@angular/core';
import { BracketMatch } from '../../models/bracket.models';

@Component({
  selector: 'app-grand-final-popup',
  templateUrl: './grand-final-popup.component.html',
  styleUrl: './grand-final-popup.component.scss'
})
export class GrandFinalPopupComponent {
  grandFinal = input.required<BracketMatch>();
  closed = signal(false);
  close(): void { this.closed.set(true); }
}
