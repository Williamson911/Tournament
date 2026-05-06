package be.technifutur.tournament.dtos;

public record MatchDTO(
        Integer id,
        String status,
        String tournamentName,
        Integer player1Id,
        Integer player2Id,
        Integer player1Score,
        Integer player2Score
) {}