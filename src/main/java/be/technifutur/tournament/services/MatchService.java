package be.technifutur.tournament.services;

import be.technifutur.tournament.daos.MatchDAO;
import jakarta.inject.Inject;

public class MatchService {
    @Inject
    private MatchDAO matchDAO;
}
