package be.technifutur.tournament.dtl;

import java.util.List;

public record BracketRoundDto(
    int roundId,
    String label,
    List<BracketMatchDto> matches
) {}
