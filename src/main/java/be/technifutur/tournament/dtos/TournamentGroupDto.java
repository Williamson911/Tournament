package be.technifutur.tournament.dtos;

import java.util.List;

public record TournamentGroupDto(
    int groupId,
    String name,
    List<GroupStandingDto> standings
) {}
