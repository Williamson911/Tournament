package be.technifutur.tournament.dtos;

import be.technifutur.tournament.entities.Match;

public record MatchDTO(
        Integer id,
        String status,
        String tournamentName,
        Integer player1Id,
        Integer player2Id,
        Integer player1Score,
        Integer player2Score
) {
    public static MatchDTO toDTO(Match m){
        return new MatchDTO(
                m.getId(),
                m.getStatus().name(),
                m.getTournament() != null ? m.getTournament().getName() : null,
                m.getPlayer1() != null ? m.getPlayer1().getId() : null,
                m.getPlayer2() != null ? m.getPlayer2().getId() : null,
                m.getPlayer1Score(),
                m.getPlayer2Score()
        );
    }
}
