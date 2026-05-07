package be.technifutur.tournament.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MatchParticipantDto(
    int playerId,
    String playerName,
    String fighterName,
    String fighterImageUrl,
    int score,
    @JsonProperty("isWinner") boolean isWinner,
    @JsonProperty("isEliminated") boolean isEliminated
) {}
