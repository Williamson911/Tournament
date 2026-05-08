package be.technifutur.tournament.dtl;

import be.technifutur.tournament.dtl.match.MatchParticipantDto;
import com.fasterxml.jackson.annotation.JsonProperty;

public record BracketMatchDto(
    int matchId,
    MatchParticipantDto participant1,
    MatchParticipantDto participant2,
    @JsonProperty("isComplete") boolean isComplete,
    @JsonProperty("isBracketReset") boolean isBracketReset
) {}
