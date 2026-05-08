package be.technifutur.tournament.dtl.fighter;

import be.technifutur.tournament.dl.entity.Fighter;

public record FighterDTO(
        Integer id,
        String name,
        String origine,
        String style,
        String image
) {
    public static FighterDTO toDTO(Fighter f){
        return new FighterDTO(
                f.getId(),
                f.getName(),
                f.getOriginCountry(),
                f.getStyle(),
                f.getImage()
        );
    }
}
