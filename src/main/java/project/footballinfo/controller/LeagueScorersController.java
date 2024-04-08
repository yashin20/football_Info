package project.footballinfo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import project.footballinfo.dto.ScorersDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
public class LeagueScorersController {

    private final ApiResponse apiResponse;


    // ============ 득점자 순위표 ============

    /**
     * 유럽 리그 득접자 순위표 엔드 포인트
     *
     * @return : ScorersDtoList
     * <p>
     * Premier League : PL : 2021
     * Bundesliga : BL1 : 2002
     * Primera Division : PD : 2014
     * Serie A : SA : 2019
     * Ligue 1 : FL1 : 2015
     */

    @GetMapping("/scorers/{leagueId}")
    @ResponseBody
    @Cacheable(value = "scorersCache", key = "#leagueId", unless = "#result == null or #result.isEmpty()")
    public List<ScorersDto> getScorersByLeagueId(@PathVariable Long leagueId) throws IOException, InterruptedException {
        String apiUrl = String.format("https://api.football-data.org/v4/competitions/%d/scorers", leagueId);
        ResponseEntity<Map> response = apiResponse.getAPIResponse(apiUrl);
        return getScorersDtoList(response);
    }

    /**
     * League Scorers Data 추출 함수
     *
     * @param response : API 응답값
     * @return : Dto 가공 리스트
     */
    private static List<ScorersDto> getScorersDtoList(ResponseEntity<Map> response) {
        Map<String, Object> body = response.getBody();

        //"body" -> "scorers"
        List<Map<String, Object>> scorers = (List<Map<String, Object>>) body.get("scorers");

        //득점자 순위 정보를 담을 리스트 초기화
        List<ScorersDto> scorersDtos = new ArrayList<>();

        //API 응답에 position 이 없음. -> 직접 순위 할당
        int position = 1;
        for (Map<String, Object> scorer : scorers) {
            Map<String, Object> player = (Map<String, Object>) scorer.get("player");
            Map<String, Object> team = (Map<String, Object>) scorer.get("team");
            //null -> 0 : Optional 사용
            Integer goals = Optional.ofNullable((Integer) scorer.get("goals")).orElse(0);
            Integer assists = Optional.ofNullable((Integer) scorer.get("assists")).orElse(0);
            Integer penalties = Optional.ofNullable((Integer) scorer.get("penalties")).orElse(0);

            ScorersDto dto = new ScorersDto(position++, (String) player.get("name"), (String) team.get("crest"), (String) team.get("name"), goals, assists, penalties);
            scorersDtos.add(dto);
        }

        return scorersDtos;
    }

}
