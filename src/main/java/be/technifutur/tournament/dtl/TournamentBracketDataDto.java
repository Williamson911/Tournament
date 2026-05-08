package be.technifutur.tournament.dtl;

import be.technifutur.tournament.dtl.match.MatchParticipantDto;

import java.util.List;

public record TournamentBracketDataDto(
        int tournamentId,
        String tournamentName,
        String status,
        boolean hasGroupStage,
        List<TournamentGroupDto> groups,
        List<BracketRoundDto> winnersBracket,
        List<BracketRoundDto> losersBracket,
        BracketMatchDto grandFinal,
        MatchParticipantDto champion
) {}
