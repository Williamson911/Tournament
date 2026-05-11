package be.technifutur.tournament.dtl.tournament;

import be.technifutur.tournament.dl.enums.TournamentActionenum;

public record TournamentActionRequestDTO(TournamentActionenum action, Object dto) {}
