package project.footballinfo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.footballinfo.dto.MatchDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class MatchController {

    private final ApiResponse apiResponse;

    /**
     * 리그 일정 엔드 포인트
     *
     * @return : MatchDto
     * <p>
     * Premier League : PL : 2021
     * Bundesliga : BL1 : 2002
     * Primera Division : PD : 2014
     * Serie A : SA : 2019
     * Ligue 1 : FL1 : 2015
     */


    //팀 별 매치 일정 검색 (teamId 기반)
    @GetMapping("/matches/team/{teamId}")
    @ResponseBody
    @Cacheable(value = "teamMatchesCache", key = "#teamId", unless = "#result == null or #result.isEmpty()")
    public List<MatchDto> getMatchesByTeamId(@PathVariable Long teamId) throws IOException, InterruptedException {
        String apiUrl = String.format("https://api.football-data.org/v4/teams/%d/matches", teamId);
        ResponseEntity<Map> response = apiResponse.getAPIResponse(apiUrl);
        return getMatchDtos(response);
    }

    //리그 별 매치 일정 검색 (leagueId 기반)
    //page, pageSize 별로 캐싱
    @GetMapping("/matches/league/{leagueId}")
    @ResponseBody
    @Cacheable(value = "leagueMatchesCache",
            key = "#leagueId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize",
            unless = "#result == null or #result.isEmpty()")
    public Page<MatchDto> getMatchesByLeagueId(@PathVariable Long leagueId, Pageable pageable) throws IOException, InterruptedException {
        String apiUrl = String.format("https://api.football-data.org/v4/competitions/%d/matches", leagueId);
        ResponseEntity<Map> response = apiResponse.getAPIResponse(apiUrl);
        List<MatchDto> matches = getMatchDtos(response);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), matches.size());

        PageImpl<MatchDto> matchDtos = new PageImpl<>(matches.subList(start, end), pageable, matches.size());
        return matchDtos;
    }

    /**
     * match 일정 추출 함수
     *
     * @param response : API 응답값
     * @return : Dto 가공 리스트
     */
    private static List<MatchDto> getMatchDtos(ResponseEntity<Map> response) {
        //여기부터 함수로 묶기
        Map<String, Object> body = response.getBody();

        //"body" -> "matches"
        List<Map<String, Object>> matches = (List<Map<String, Object>>) body.get("matches");

        List<MatchDto> matchDtos = new ArrayList<>();

        for (Map<String, Object> match : matches) {
            Map<String, Object> competition = (Map<String, Object>) match.get("competition");
            Map<String, Object> homeTeam = (Map<String, Object>) match.get("homeTeam");
            Map<String, Object> awayTeam = (Map<String, Object>) match.get("awayTeam");
            Map<String, Object> score = (Map<String, Object>) match.get("score");
            Map<String, Object> fullTime = (Map<String, Object>) score.get("fullTime");

            //utcDate : 경기 일자
            String utcDate = (String) match.get("utcDate");
            //matchEmblem
            String emblem = (String) competition.get("emblem");
            //status
            String status = (String) match.get("status");
            //homeTeamName
            String homeTeamName = (String) homeTeam.get("name");
            //homeTeamCrest
            String homeTeamCrest = (String) homeTeam.get("crest");
            //awayTeamName
            String awayTeamName = (String) awayTeam.get("name");
            //awayTeamCrest
            String awayTeamCrest = (String) awayTeam.get("crest");
            //homeTeamScore
            Integer homeTeamScore = (Integer) fullTime.get("home");
            //awayTeamScore
            Integer awayTeamScore = (Integer) fullTime.get("away");

            MatchDto matchDto = new MatchDto(utcDate, emblem, status, homeTeamName, homeTeamCrest, awayTeamName, awayTeamCrest, homeTeamScore, awayTeamScore);

            matchDtos.add(matchDto);
        }
        return matchDtos;
    }
}
