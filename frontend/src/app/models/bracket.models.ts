export interface TournamentParticipant {
  playerId: number;
  playerName: string;
  fighterName: string;
  fighterImageUrl: string;
}

export interface GroupStanding {
  rank: number;
  participant: TournamentParticipant;
  wins: number;
  losses: number;
  points: number;
  qualified: boolean;
}

export interface TournamentGroup {
  groupId: number;
  name: string;
  standings: GroupStanding[];
}

export interface MatchParticipant extends TournamentParticipant {
  score: number;
  isWinner: boolean;
  isEliminated: boolean;
}

export interface BracketMatch {
  matchId: number;
  participant1: MatchParticipant | null;
  participant2: MatchParticipant | null;
  isComplete: boolean;
}

export interface BracketRound {
  roundId: number;
  label: string;
  matches: BracketMatch[];
}

export interface TournamentBracketData {
  tournamentId: number;
  tournamentName: string;
  status: string;
  hasGroupStage: boolean;
  groups: TournamentGroup[];
  winnersBracket: BracketRound[];
  losersBracket: BracketRound[];
  grandFinal: BracketMatch | null;
  champion: MatchParticipant | null;
}
