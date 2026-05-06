package be.technifutur.tournament.dtos;

import java.util.List;

public record BracketRoundDto(
    int roundId,
    String label,
    List<BracketMatchDto> matches
) {}
