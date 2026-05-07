package be.technifutur.tournament.dtos;

import java.util.List;

public record TournamentBracketDataDto(
    int tournamentId,
    String tournamentName,
    boolean hasGroupStage,
    List<TournamentGroupDto> groups,
    List<BracketRoundDto> winnersBracket,
    List<BracketRoundDto> losersBracket,
    BracketMatchDto grandFinal,
    MatchParticipantDto champion
) {}
