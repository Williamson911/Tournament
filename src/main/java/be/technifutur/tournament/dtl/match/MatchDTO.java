package be.technifutur.tournament.dtl.match;

import be.technifutur.tournament.dtl.player.PlayerDTO;
import be.technifutur.tournament.dtl.tournament.TournamentDTO;
import be.technifutur.tournament.dl.entity.Match;

public record MatchDTO(
        Integer id,
        String status,
        TournamentDTO tournament,
        PlayerDTO player1,
        PlayerDTO player2,
        Integer player1Score,
        Integer player2Score
) {
    public static MatchDTO toDTO(Match m){
        return new MatchDTO(
                m.getId(),
                m.getStatus().name(),
                m.getTournament() != null ? TournamentDTO.toDTO(m.getTournament()): null,
                m.getPlayer1() != null ? PlayerDTO.toDTO(m.getPlayer1()): null,
                m.getPlayer2() != null ?  PlayerDTO.toDTO(m.getPlayer2()): null,
                m.getPlayer1Score(),
                m.getPlayer2Score()
        );
    }
}
