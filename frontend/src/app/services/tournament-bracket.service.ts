import { Injectable, WritableSignal, inject } from "@angular/core";
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

    nextRound(tournamentId: number, winnerSet: Set<number> = new Set<number>()): Observable<TournamentBracketData> {
        if (winnerSet.size) {
            return this.http.post<TournamentBracketData>(`/api/tournaments`, {
                action: "APPLY_BRAWL",
                dto: { tournamentId, winnerSet: Array.from(winnerSet) }
            });
        } else {
            return this.http.post<TournamentBracketData>(`/api/tournaments`, {
                action: "SIMULATE_NEXT_ROUND",
                dto: { tournamentId, winnerSet: [] }
            });
        }
    }

    oneMatch(tournamentId: number, winnerSet: Set<number> = new Set<number>()): Observable<TournamentBracketData> {
        if (winnerSet.size) {
            return this.http.post<TournamentBracketData>(`/api/tournaments`, {
                action: "APPLY_DUEL",
                dto: { tournamentId, winnerSet: Array.from(winnerSet) }
            });
        } else {
            return this.http.post<TournamentBracketData>(`/api/tournaments`, {
                action: "SIMULATE_ONE_MATCH",
                dto: { tournamentId, winnerSet: [] }
            });
        }
    }

    resetTournament(tournamentId: number): Observable<any> {
        return this.http.post(`/api/tournaments`, {
            action: "RESET",
            dto: { tournamentId }
        });
    }
}
