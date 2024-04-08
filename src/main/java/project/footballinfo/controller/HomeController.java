package project.footballinfo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class HomeController {

    /**
     * 외부 API 를 이용
     * 화면에 출력
     */

    @GetMapping("/")
    public String home(Model model) throws IOException, InterruptedException {
        //국기 URL 리스트
        List<String> flagUrls = Arrays.asList(
                "https://crests.football-data.org/770.svg", // PL
                "https://crests.football-data.org/759.svg", // BL1
                "https://crests.football-data.org/760.svg", // PD
                "https://crests.football-data.org/784.svg", // SA
                "https://crests.football-data.org/773.svg", // FL1
                "https://crests.football-data.org/CL.png", // UEFA Champions League : 2001
                "https://crests.football-data.org/EL.png", // UEFA Europa League : 2146
                "https://crests.football-data.org/UCL.png" // UEFA Conference League : 2154
        );

        model.addAttribute("flagUrls", flagUrls);

        return "index";
    }
}
