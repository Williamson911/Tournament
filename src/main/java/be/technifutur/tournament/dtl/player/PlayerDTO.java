package be.technifutur.tournament.dtl.player;

import be.technifutur.tournament.dl.entities.Player;

public record PlayerDTO(
        Integer id,
        String username,
        String email,
        int age,
        int elo,
        String image
) {
    public static PlayerDTO toDTO(Player p){
        return new PlayerDTO(
                p.getId(),
                p.getUsername(),
                p.getEmail(),
                p.getAge(),
                p.getElo(),
                p.getImage()
        );
    }
}
