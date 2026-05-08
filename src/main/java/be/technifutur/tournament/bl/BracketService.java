package be.technifutur.tournament.bl;

import be.technifutur.tournament.dal.MatchDAO;
import be.technifutur.tournament.dal.TournamentDAO;
import be.technifutur.tournament.dtl.*;
import be.technifutur.tournament.dtl.match.MatchParticipantDto;
import be.technifutur.tournament.dl.entity.Match;
import be.technifutur.tournament.dl.entity.Player;
import be.technifutur.tournament.dl.enums.BracketStage;
import be.technifutur.tournament.dl.enums.MatchStatus;
import be.technifutur.tournament.dal.MatchDAO;
import be.technifutur.tournament.dal.TournamentDAO;
import be.technifutur.tournament.dtl.*;
import be.technifutur.tournament.dl.entity.Match;
import be.technifutur.tournament.dl.entity.Player;
import be.technifutur.tournament.dl.enums.BracketStage;
import be.technifutur.tournament.dl.enums.MatchStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class BracketService {

    @Inject
    private TournamentDAO tournamentDAO;

    @Inject
    private MatchDAO matchDAO;

    public TournamentBracketDataDto buildBracketData(int tournamentId) {
        var tournament = tournamentDAO.findById(tournamentId)
            .orElseThrow(() -> new NotFoundException("Tournament " + tournamentId + " not found"));

        List<Match> matches = matchDAO.findByTournamentWithPlayers(tournamentId);

        List<Match> wbMatches = matches.stream()
            .filter(m -> m.getBracketStage() == BracketStage.WINNERS_BRACKET).toList();
        List<Match> lbMatches = matches.stream()
            .filter(m -> m.getBracketStage() == BracketStage.LOSERS_BRACKET).toList();
        Optional<Match> gfMatch = matches.stream()
            .filter(m -> m.getBracketStage() == BracketStage.GRAND_FINAL).findFirst();
        List<Match> groupMatches = matches.stream()
            .filter(m -> m.getBracketStage() == BracketStage.GROUP_STAGE).toList();

        List<BracketRoundDto> winnersBracket = buildRounds(wbMatches, "WB");
        List<BracketRoundDto> losersBracket  = buildRounds(lbMatches, "LB");
        BracketMatchDto grandFinal = gfMatch.map(this::toMatchDto).orElse(null);
        MatchParticipantDto champion = gfMatch
            .filter(m -> m.getStatus() == MatchStatus.FINISHED)
            .map(m -> m.getPlayer1Score() > m.getPlayer2Score()
                ? toParticipantDto(m.getPlayer1(), m.getPlayer1Score(), true, false)
                : toParticipantDto(m.getPlayer2(), m.getPlayer2Score(), true, false))
            .orElse(null);

        boolean hasGroupStage = tournament.isHasGroupStage();
        List<TournamentGroupDto> groups = buildGroups(groupMatches);

        return new TournamentBracketDataDto(
            tournament.getId(), tournament.getName(), tournament.getStatus().name(), hasGroupStage,
            groups, winnersBracket, losersBracket,
            grandFinal, champion
        );
    }

    private List<BracketRoundDto> buildRounds(List<Match> matches, String prefix) {
        if (matches.isEmpty()) return List.of();

        Map<Integer, List<Match>> byRound = matches.stream()
            .collect(Collectors.groupingBy(m -> m.getRoundNumber() != null ? m.getRoundNumber() : 0));

        int maxRound = byRound.keySet().stream().mapToInt(i -> i).max().orElse(0);

        return byRound.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> {
                int r = e.getKey();
                String label = prefix + " " + roundLabel(r, maxRound);
                List<BracketMatchDto> matchDtos = e.getValue().stream()
                    .map(this::toMatchDto).toList();
                return new BracketRoundDto(r, label, matchDtos);
            })
            .toList();
    }

    private String roundLabel(int round, int max) {
        if (round == max) return "FINALE";
        if (round == max - 1) return "DEMI-FINALE";
        return "ROUND " + round;
    }

    private BracketMatchDto toMatchDto(Match m) {
        boolean complete = m.getStatus() == MatchStatus.FINISHED;
        BracketStage stage = m.getBracketStage();
        boolean p1Wins = m.getPlayer1Score() != null && m.getPlayer2Score() != null
            && m.getPlayer1Score() > m.getPlayer2Score();

        MatchParticipantDto p1 = m.getPlayer1() != null
            ? toParticipantDto(m.getPlayer1(),
                m.getPlayer1Score() != null ? m.getPlayer1Score() : 0,
                complete && p1Wins,
                complete && isEliminated(stage, !p1Wins))
            : null;
        MatchParticipantDto p2 = m.getPlayer2() != null
            ? toParticipantDto(m.getPlayer2(),
                m.getPlayer2Score() != null ? m.getPlayer2Score() : 0,
                complete && !p1Wins,
                complete && isEliminated(stage, p1Wins))
            : null;
        return new BracketMatchDto(m.getId(), p1, p2, complete);
    }

    private boolean isEliminated(BracketStage stage, boolean isLoser) {
        if (!isLoser) return false;
        return stage == BracketStage.LOSERS_BRACKET || stage == BracketStage.GRAND_FINAL;
    }

    private MatchParticipantDto toParticipantDto(Player player, int score,
                                                  boolean isWinner, boolean isEliminated) {
        var fighter = player.getFighterMain();
        return new MatchParticipantDto(
            player.getId(), player.getUsername(),
            fighter.getName(), fighter.getImage(),
            score, isWinner, isEliminated
        );
    }

    private List<TournamentGroupDto> buildGroups(List<Match> groupMatches) {
        Map<String, List<Match>> byGroup = groupMatches.stream()
            .collect(Collectors.groupingBy(m ->
                m.getBracketPosition() != null ? m.getBracketPosition() : "GROUP_A"));

        int idCounter = 1;
        List<TournamentGroupDto> result = new ArrayList<>();
        for (var entry : byGroup.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).toList()) {
            result.add(new TournamentGroupDto(
                idCounter++, entry.getKey(), computeStandings(entry.getValue())));
        }
        return result;
    }

    private List<GroupStandingDto> computeStandings(List<Match> matches) {
        Map<Player, int[]> stats = new LinkedHashMap<>();

        for (Match m : matches) {
            stats.computeIfAbsent(m.getPlayer1(), p -> new int[3]);
            stats.computeIfAbsent(m.getPlayer2(), p -> new int[3]);
            if (m.getStatus() != MatchStatus.FINISHED) continue;

            boolean p1Wins = m.getPlayer1Score() > m.getPlayer2Score();
            Player winner = p1Wins ? m.getPlayer1() : m.getPlayer2();
            Player loser  = p1Wins ? m.getPlayer2() : m.getPlayer1();

            stats.get(winner)[0]++;
            stats.get(winner)[2] += 3;
            stats.get(loser)[1]++;
        }

        var sorted = stats.entrySet().stream()
            .sorted((a, b) -> {
                int cmp = Integer.compare(b.getValue()[2], a.getValue()[2]);
                return cmp != 0 ? cmp : Integer.compare(b.getValue()[0], a.getValue()[0]);
            })
            .toList();

        List<GroupStandingDto> standings = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Player p = sorted.get(i).getKey();
            int[] s = sorted.get(i).getValue();
            var participant = new TournamentParticipantDto(
                p.getId(), p.getUsername(),
                p.getFighterMain().getName(), p.getFighterMain().getImage()
            );
            standings.add(new GroupStandingDto(i + 1, participant, s[0], s[1], s[2], i < 2));
        }
        return standings;
    }
}
