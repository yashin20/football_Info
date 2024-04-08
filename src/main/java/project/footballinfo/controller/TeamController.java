package project.footballinfo.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class TeamController {

    @GetMapping("/teamInfo")
    public String teamInfo(@RequestParam(name = "teamId") Integer teamId, Model model) {
        model.addAttribute("teamId", teamId);
        return "teamInfo";
    }
}
