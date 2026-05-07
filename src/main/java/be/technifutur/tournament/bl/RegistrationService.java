package be.technifutur.tournament.bl;

import be.technifutur.tournament.dal.PlayerDao;
import be.technifutur.tournament.dal.RegistrationDAO;
import be.technifutur.tournament.dal.TournamentDAO;
import be.technifutur.tournament.dl.entity.Player;
import jakarta.inject.Inject;

public class RegistrationService {

    @Inject
    private RegistrationDAO registrationDAO;

    @Inject
    private PlayerDao playerDao;

    @Inject
    private TournamentDAO tournamentDAO;


}
