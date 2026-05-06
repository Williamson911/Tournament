import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'tournament/:id/bracket',
    loadComponent: () =>
      import('./bracket/bracket-page/bracket-page.component').then(m => m.BracketPageComponent)
  }
];
