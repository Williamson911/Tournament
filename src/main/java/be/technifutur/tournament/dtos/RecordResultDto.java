package be.technifutur.tournament.dtos;

import be.technifutur.tournament.enums.FinishType;

public record RecordResultDto(int player1Score, int player2Score, FinishType finishType) {}
