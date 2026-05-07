package be.technifutur.tournament.services;

import be.technifutur.tournament.entities.Player;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class GroupStageService {

    public List<Player> selectQualifiers(List<Player> players) {
        List<Player> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled);
        List<Player> qualifiers = new ArrayList<>();
        for (int i = 0; i < shuffled.size(); i += 4) {
            List<Player> group = new ArrayList<>(shuffled.subList(i, i + 4));
            Collections.shuffle(group);
            qualifiers.add(group.get(0));
            qualifiers.add(group.get(1));
        }
        return qualifiers;
    }
}
