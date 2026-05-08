package be.technifutur.tournament.dtl;

import be.technifutur.tournament.dtl.match.MatchParticipantDto;

import java.util.List;

public record TournamentBracketDataDto(
    int tournamentId,
    String tournamentName,
    boolean hasGroupStage,
    List<TournamentGroupDto> groups,
    List<BracketRoundDto> winnersBracket,
    List<BracketRoundDto> losersBracket,
    BracketMatchDto grandFinal,
    BracketMatchDto bracketReset,
    MatchParticipantDto champion
) {}
