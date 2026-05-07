package be.technifutur.tournament.dtl;

import be.technifutur.tournament.dl.enums.FinishType;

public record RecordResultDto(int player1Score, int player2Score, FinishType finishType) {}
