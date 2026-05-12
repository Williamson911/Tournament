package be.technifutur.tournament.bl;

import be.technifutur.tournament.dal.PlayerDao;
import be.technifutur.tournament.dal.RegistrationDAO;
import be.technifutur.tournament.dal.TournamentDAO;
import be.technifutur.tournament.dl.entity.Player;
import be.technifutur.tournament.dl.entity.Registration;
import be.technifutur.tournament.dl.entity.Tournament;
import be.technifutur.tournament.dl.enums.RegistrationStatus;
import be.technifutur.tournament.dl.enums.TournamentStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class RegistrationService {

    @Inject
    private RegistrationDAO registrationDAO;

    @Inject
    private PlayerDao playerDao;

    @Inject
    private TournamentDAO tournamentDAO;

    //Enregistrement d'un joueur à un tournoi
    public void registerPlayerToTournament(Integer playerId, Integer tournamentId) {
        //vérifie si le joueur existe bien
        Player player = playerDao.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with id: " + playerId));

        //vérifie si le joueur est déjà inscrit au tournoi
        if (registrationDAO.existsByPlayerIdAndTournamentId(playerId, tournamentId)) {
            throw new IllegalStateException("Player is already registered for this tournament.");
        }

        //vérifie si le tournoi existe bien
        Tournament tournament = tournamentDAO.findById(tournamentId).orElseThrow(()
                -> new IllegalArgumentException("Tournament not found"));

        //vérifie si le tournoi auquel on veut s'inscrire est OPEN.
        if (tournament.getStatus() != TournamentStatus.OPEN) {
            throw new IllegalStateException("Tournament is not open for registration.");
        }

        registrationDAO.registerPlayerToTournament(playerId, tournamentId);
    }

    //Verification si un joueur est déjà inscrit à un tournoi
    public boolean isPlayerRegisteredForTournament(Integer playerId, Integer tournamentId) {
        return registrationDAO.existsByPlayerIdAndTournamentId(playerId, tournamentId);
    }

    //Désinscription joueur d'un tournoi
    public void unregisterPlayerFromTournament(Integer playerId, Integer tournamentId) {
        Registration registration = registrationDAO.findByPlayerAndTournament(playerId, tournamentId)
                .orElseThrow(() -> new IllegalStateException("Player is not registered for this tournament."));

        Tournament tournament = tournamentDAO.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));

        if (registration.getRegistrationStatus() == RegistrationStatus.DISQUALIFIED)
            throw new IllegalStateException("Cannot unregister a disqualified player.");

        if (tournament.getStatus() != TournamentStatus.OPEN)
            throw new IllegalStateException("Cannot unregister from a tournament with status: " + tournament.getStatus());

        registrationDAO.unregisterPlayerFromTournament(playerId, tournamentId);
    }

    //Mise à jour du status pour un tournoi (ex : de PENDING à CONFIRMED)
    public void updateStatusRegistration(Integer tournamentId, RegistrationStatus newStatus) {
        if (!registrationDAO.existsByTournamentId(tournamentId)) {
            throw new IllegalStateException("No registrations found for this tournament.");
        }

        registrationDAO.updateStatusRegistration(tournamentId, newStatus);
    }

    //Compte le nombre de joueurs inscrit à un tournoi
    public int countPlayersRegisteredForTournament(Integer tournamentId) {
        return registrationDAO.countByTournamentId(tournamentId);
    }

    //Revoie la liste des joueurs inscrits à un tournoi
    public List<Player> getRegisteredPlayersForTournament (Integer tournamentId) {
        return registrationDAO.getRegisteredPlayersForTournament(tournamentId);
    }

    //Renvoi la liste de tous les tournois auquels est inscrit un joueur
    public List<Tournament> getRegisteredTournamentsForPlayer (Integer playerId) {
        return registrationDAO.getRegisteredTournamentForPlayers(playerId);
    }
}
