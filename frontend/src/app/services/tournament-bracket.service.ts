import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { TournamentBracketData } from "../models/bracket.models";

@Injectable({ providedIn: "root" })
export class TournamentBracketService {
    private http = inject(HttpClient);

    getBracket(tournamentId: number): Observable<TournamentBracketData> {
        return this.http.get<TournamentBracketData>(`/api/bracket/${tournamentId}/bracket`);
    }

    launchGroupStage(tournamentId: number): Observable<any> {
        return this.http.post(`/api/tournaments`, {
          action: "LAUNCH_GROUP_STAGE",
            dto: { tournamentId }
        });
    }

    generateBracket(tournamentId: number): Observable<any> {
        return this.http.post(`/api/tournaments`, {
          action: "GENERATE_BRACKET",
            dto: { tournamentId }
        });
    }

    simulateNextRound(tournamentId: number): Observable<TournamentBracketData> {
        return this.http.post<TournamentBracketData>(`/api/tournaments`, {
          action: "SIMULATE_NEXT_ROUND",
            dto: { tournamentId }
        });
    }

    simulateOneMatch(tournamentId: number): Observable<TournamentBracketData> {
        return this.http.post<TournamentBracketData>(`/api/tournaments`, {
            action: "SIMULATE_ONE_MATCH",
            dto: { tournamentId }
        });
    }

    resetTournament(tournamentId: number): Observable<any> {
        return this.http.post(`/api/tournaments`, {
            action: "RESET",
            dto: { tournamentId }
        });
    }
}
