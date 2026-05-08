package be.technifutur.tournament.dtl;

import java.util.List;

public record TournamentGroupDto(
    int groupId,
    String name,
    List<GroupStandingDto> standings
) {}
