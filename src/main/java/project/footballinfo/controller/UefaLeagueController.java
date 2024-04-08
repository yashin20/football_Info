package project.footballinfo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import static project.footballinfo.controller.UefaLeagueService.*;

@Controller
@RequiredArgsConstructor
public class UefaLeagueController {

    @Autowired
    private UefaLeagueService uefaLeagueService;

    @GetMapping("/UEFA/standings")
    public ResponseEntity<CompetitionInfo> getStandings() {
        CompetitionInfo competitionInfo = uefaLeagueService.getCompetitionStandings();
        return ResponseEntity.ok(competitionInfo);
    }


    @GetMapping("/uefaLeague/groupStage")
    public String uefaLeagueGroupStage(Model model) {
        CompetitionInfo competitionInfo = uefaLeagueService.getCompetitionStandings();
        model.addAttribute("standings", competitionInfo);
        return "UEFA_League/groupStage";
    }

    @GetMapping("/uefaLeague/tournament")
    public String uefaLeagueTournament(Model model) {
        Result tournamentMatches = uefaLeagueService.getTournamentMatches();
        model.addAttribute("LAST_16", tournamentMatches.getData1());
        model.addAttribute("QUARTER_FINALS", tournamentMatches.getData2());
        model.addAttribute("SEMI_FINALS", tournamentMatches.getData3());
        model.addAttribute("FINAL", tournamentMatches.getData4());
        return "UEFA_League/tournament";
    }
}
