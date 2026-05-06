package be.technifutur.tournament.services;

import be.technifutur.tournament.daos.PlayerDao;
import be.technifutur.tournament.daos.RegistrationDAO;
import be.technifutur.tournament.daos.TournamentDAO;
import be.technifutur.tournament.entities.Player;
import jakarta.inject.Inject;

public class RegistrationService {

    @Inject
    private RegistrationDAO registrationDAO;

    @Inject
    private PlayerDao playerDao;

    @Inject
    private TournamentDAO tournamentDAO;


}
