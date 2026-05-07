import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TournamentBracketService } from './tournament-bracket.service';
import { TournamentBracketData } from '../models/bracket.models';

const MOCK: TournamentBracketData = {
  tournamentId: 1,
  tournamentName: 'Iron Fist Grand Prix',
  hasGroupStage: false,
  groups: [],
  winnersBracket: [{
    roundId: 1, label: 'WB Semi-Finals',
    matches: [{
      matchId: 1,
      participant1: { playerId: 1, playerName: 'MaxCombo', fighterName: 'Jin Kazama', fighterImageUrl: '', score: 2, isWinner: true, isEliminated: false },
      participant2: { playerId: 2, playerName: 'DevilFist', fighterName: 'Kazuya', fighterImageUrl: '', score: 1, isWinner: false, isEliminated: false },
      isComplete: true
    }]
  }],
  losersBracket: [],
  grandFinal: {
    matchId: 10,
    participant1: { playerId: 1, playerName: 'MaxCombo', fighterName: 'Jin Kazama', fighterImageUrl: '', score: 3, isWinner: true, isEliminated: false },
    participant2: { playerId: 3, playerName: 'JaggedMask', fighterName: 'King', fighterImageUrl: '', score: 2, isWinner: false, isEliminated: false },
    isComplete: true
  },
  champion: { playerId: 1, playerName: 'MaxCombo', fighterName: 'Jin Kazama', fighterImageUrl: '', score: 3, isWinner: true, isEliminated: false }
};

describe('TournamentBracketService', () => {
  let service: TournamentBracketService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TournamentBracketService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(TournamentBracketService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('fetches bracket data via GET /api/tournaments/:id/bracket', () => {
    let result: TournamentBracketData | undefined;
    service.getBracket(1).subscribe(d => (result = d));
    const req = http.expectOne('/api/tournaments/1/bracket');
    expect(req.request.method).toBe('GET');
    req.flush(MOCK);
    expect(result).toEqual(MOCK);
  });
});
