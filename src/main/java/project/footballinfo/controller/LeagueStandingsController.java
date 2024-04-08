package project.footballinfo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import project.footballinfo.dto.StandingsDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class LeagueStandingsController {

    private final ApiResponse apiResponse;

    /**
     * 유럽 리그 순위표 엔드 포인트
     *
     * @return : StandingsDtoList
     * <p>
     * Premier League : PL : 2021
     * Bundesliga : BL1 : 2002
     * Primera Division : PD : 2014
     * Serie A : SA : 2019
     * Ligue 1 : FL1 : 2015
     */


    @GetMapping("/standings/{leagueId}")
    @ResponseBody
    @Cacheable(value = "standingsCache", key = "#leagueId", unless = "#result == null or #result.isEmpty()")
    public List<StandingsDto> getStandingsByLeagueId(@PathVariable Long leagueId) throws IOException, InterruptedException {
        String apiUrl = String.format("https://api.football-data.org/v4/competitions/%d/standings", leagueId);
        ResponseEntity<Map> response = apiResponse.getAPIResponse(apiUrl);
        return getStandingsDtoList(response);
    }


    /**
     * League Standings Data 추출 함수
     *
     * @param response : API 응답값
     * @return : Dto 가공 리스트
     */
    private static List<StandingsDto> getStandingsDtoList(ResponseEntity<Map> response) {
        Map<String, Object> body = response.getBody();

        // "standings" -> [0] -> "table" 순으로 접근
        List<Map<String, Object>> standings = (List<Map<String, Object>>) body.get("standings");
        List<Map<String, Object>> table = (List<Map<String, Object>>) standings.get(0).get("table");

        //팀 순위 정보를 담을 리스트 초기화
        List<StandingsDto> standingsDtos = new ArrayList<>();

        //'table' 배열 순회하며 팀 순위 정보 추출
        for (Map<String, Object> entry : table) {
            Map<String, Object> team = (Map<String, Object>) entry.get("team");
            Integer id = (Integer) team.get("id");// teamId
            Integer position = (Integer) entry.get("position"); //순위
            String crest = (String) team.get("crest"); //로고
            String teamName = (String) team.get("name"); //팀명
            Integer playedGames = (Integer) entry.get("playedGames"); //경기수
            Integer points = (Integer) entry.get("points"); //승점
            Integer won = (Integer) entry.get("won"); //승
            Integer draw = (Integer) entry.get("draw");//무
            Integer lost = (Integer) entry.get("lost");//패
            Integer goalsFor = (Integer) entry.get("goalsFor");//득점
            Integer goalsAgainst = (Integer) entry.get("goalsAgainst");//실점
            Integer goalDifference = (Integer) entry.get("goalDifference");//득실차

            //추출한 정보를 기반으로 StandingsDto 객체 생성 및 리스트 추가
            standingsDtos.add(new StandingsDto(id, position, crest, teamName, playedGames, points, won, draw, lost, goalsFor, goalsAgainst, goalDifference));
        }
        return standingsDtos;
    }
}
