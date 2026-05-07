import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TournamentBracketData } from '../models/bracket.models';

@Injectable({ providedIn: 'root' })
export class TournamentBracketService {
  private http = inject(HttpClient);

  getBracket(tournamentId: number): Observable<TournamentBracketData> {
    return this.http.get<TournamentBracketData>(`/api/tournaments/${tournamentId}/bracket`);
  }

  launchGroupStage(tournamentId: number): Observable<any> {
    return this.http.post(`/api/tournaments/${tournamentId}/launch-group-stage`, {});
  }

  generateBracket(tournamentId: number): Observable<any> {
    return this.http.post(`/api/tournaments/${tournamentId}/generate-bracket`, {});
  }
}
