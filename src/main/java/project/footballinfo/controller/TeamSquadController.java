package project.footballinfo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import project.footballinfo.dto.PlayerDto;
import project.footballinfo.dto.SquadDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class TeamSquadController {

    private final ApiResponse apiResponse;


    /**
     * 팀 스쿼드 엔드 포인트
     * 팀 스쿼드 + 팀이 참가중인 대회
     *
     * @return : SquadDto
     */

    @GetMapping("/squad/{teamId}")
    @ResponseBody
    @Cacheable(value = "squadCache", key = "#teamId")
    public SquadDto getSquadByTeamId(@PathVariable Long teamId) throws IOException, InterruptedException {
        String apiUrl = String.format("https://api.football-data.org/v4/teams/%d", teamId);
        ResponseEntity<Map> response = apiResponse.getAPIResponse(apiUrl);
        return getSquadDto(response);
    }


    /**
     * Team Squad Data 추출 함수
     *
     * @param response : API 응답값
     * @return : SquadDto
     */
    private static SquadDto getSquadDto(ResponseEntity<Map> response) {
        //여기부터..
        Map<String, Object> body = response.getBody();

        // 0. 엠블럼, 팀명
        String crest = (String) body.get("crest");
        String teamName = (String) body.get("name");

        // 1. 참가중인 대회 엠블럼
        List<String> emblem = new ArrayList<>(); // 참가중인 대회 엠블럼 리스트
        List<Map<String, Object>> runningCompetitions = (List<Map<String, Object>>) body.get("runningCompetitions");
        for (Map<String, Object> runningCompetition : runningCompetitions) {
            emblem.add((String) runningCompetition.get("emblem"));
        }

        // 2. 코치 (이름, 생년월일, 국적)
        Map<String, Object> coach = (Map<String, Object>) body.get("coach");
        String coachName = (String) coach.get("name");
        String coachDateOfBirth = (String) coach.get("dateOfBirth");
        String coachNationality = (String) coach.get("nationality");


        // 3. 선수 명단 (name, position, dateOfBirth, nationality)
        List<PlayerDto> playerDtos = new ArrayList<>(); // 선수 명단 리스트
        List<Map<String, Object>> squad = (List<Map<String, Object>>) body.get("squad");
        for (Map<String, Object> player : squad) {
            //Integer -> Long 변환
            Integer integerId = (Integer) player.get("id");
            Long id = integerId.longValue();

            String name = (String) player.get("name");
            String position = (String) player.get("position");
            String dateOfBirth = (String) player.get("dateOfBirth");
            String nationality = (String) player.get("nationality");
            playerDtos.add(new PlayerDto(id, name, position, dateOfBirth, nationality));
        }

        return new SquadDto(crest, teamName, emblem, coachName, coachDateOfBirth, coachNationality, playerDtos);
    }
}
