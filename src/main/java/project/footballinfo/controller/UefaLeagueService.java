package project.footballinfo.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import project.footballinfo.dto.MatchDto;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UefaLeagueService {

    private final ApiResponse apiResponse;


    @Data
    @AllArgsConstructor
    public static class TeamInfo { //챔피언스리그 조별리그 팀 정보
        private Integer id;
        private String name;
        private String crest; // 엠블럼

        private Integer playedGames; //경기수
        private Integer won; //승리
        private Integer draw; //무승부
        private Integer lost; //패배
        private Integer points; //승점
        private Integer goalsFor; //득점
        private Integer goalsAgainst; //실점
        private Integer goalDifference; //득실차
    }


    @Data
    @NoArgsConstructor
    public static class GroupStanding { // 그룹별 순위 정보를 담는 DTO
        private String group;
        private List<TeamInfo> teams;
    }


    @Data
    @NoArgsConstructor
    public static class CompetitionInfo { // 전체 대회 정보를 담는 DTO
        private String competitionName;
        private List<GroupStanding> standings;
    }


    /** 조별리그 */
    public CompetitionInfo getCompetitionStandings() {
        //API 요청/응답
        String apiUrl = String.format("https://api.football-data.org/v4/competitions/2001/standings");
        ResponseEntity<Map> response = apiResponse.getAPIResponse(apiUrl);

        CompetitionInfo competitionInfo = new CompetitionInfo();
        competitionInfo.setCompetitionName("UEFA Champions League");

        List<Map<String, Object>> standings = (List<Map<String, Object>>) response.getBody().get("standings");

        List<GroupStanding> groupStandings = new ArrayList<>();

        //Group_A ~ H -> 총 8개
        for (Map<String, Object> standing : standings) {
            GroupStanding groupStanding = new GroupStanding();

            String group = (String) standing.get("group"); //GroupStanding - group
            groupStanding.setGroup(group);
            List<TeamInfo> teamInfoList = new ArrayList<>();

            List<Map<String, Object>> teams = (List<Map<String, Object>>) standing.get("table");
            for (Map<String, Object> team : teams) {
                Map<String, Object> map = (Map<String, Object>) team.get("team");
                Integer id = (Integer) map.get("id"); //id
                String name = (String) map.get("name"); //name
                String crest = (String) map.get("crest"); //crest

                Integer playedGames = (Integer) team.get("playedGames");
                Integer won = (Integer) team.get("won");
                Integer draw = (Integer) team.get("draw");
                Integer lost = (Integer) team.get("lost");
                Integer points = (Integer) team.get("points");
                Integer goalsFor = (Integer) team.get("goalsFor");
                Integer goalsAgainst = (Integer) team.get("goalsAgainst");
                Integer goalDifference = (Integer) team.get("goalDifference");

                TeamInfo teamInfo = new TeamInfo(id, name, crest,
                        playedGames, won, draw, lost, points, goalsFor, goalsAgainst, goalDifference);
                teamInfoList.add(teamInfo);
            }
            groupStanding.setTeams(teamInfoList);
            groupStandings.add(groupStanding);
        }

        // 최종적으로 CompetitionInfo 객체에 standings 정보를 설정
        competitionInfo.setStandings(groupStandings);

        return competitionInfo;
    }

    private static List<MatchDto> getMatchDtos(List<Map<String, Object>> matches) {

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


    /** 토너먼트 */

    public Result getTournamentMatches() {
        String apiUrl = String.format("https://api.football-data.org/v4/competitions/2001/matches");

        ResponseEntity<Map> response = apiResponse.getAPIResponse(apiUrl);
        List<Map<String, Object>> matches = (List<Map<String, Object>>) response.getBody().get("matches");

        //stage="LAST_16" 필터링 - 16강
        //LAST_16_matches : 16강 경기 (List<Map<String, Object>>)
        List<Map<String, Object>> LAST_16_matches = matches.stream()
                .filter(match -> "LAST_16".equals(match.get("stage")))
                .collect(Collectors.toList());
        //16강 대진
        Map<Integer, Integer> LAST_16_MatchUp = getMatchUp(LAST_16_matches);
        //16강 1,2차전 누적 스코어
        Map<Integer, TeamScore> LAST_16_TeamScoreList = getTeamScoreList(LAST_16_matches);
        //16강 결과 추출
        List<StageScoreDto> LAST_16 = getResult(LAST_16_MatchUp, LAST_16_TeamScoreList);


        //stage="QUARTER_FINALS" 필터링 - 8강
        List<Map<String, Object>> QUARTER_FINALS_matches = matches.stream()
                .filter(match -> "QUARTER_FINALS".equals(match.get("stage")))
                .collect(Collectors.toList());
        //16강 대진
        Map<Integer, Integer> QUARTER_FINALS_MatchUp = getMatchUp(QUARTER_FINALS_matches);
        //16강 1,2차전 누적 스코어
        Map<Integer, TeamScore> QUARTER_FINALS_TeamScoreList = getTeamScoreList(QUARTER_FINALS_matches);
        //16강 결과 추출
        List<StageScoreDto> QUARTER_FINALS = getResult(QUARTER_FINALS_MatchUp, QUARTER_FINALS_TeamScoreList);


        //stage="SEMI_FINALS" 필터링 - 4강
        List<Map<String, Object>> SEMI_FINALS_matches = matches.stream()
                .filter(match -> "SEMI_FINALS".equals(match.get("stage")))
                .collect(Collectors.toList());
        //16강 대진
        Map<Integer, Integer> SEMI_FINALS_MatchUp = getMatchUp(SEMI_FINALS_matches);
        //16강 1,2차전 누적 스코어
        Map<Integer, TeamScore> SEMI_FINALS_TeamScoreList = getTeamScoreList(SEMI_FINALS_matches);
        //16강 결과 추출
        List<StageScoreDto> SEMI_FINALS = getResult(SEMI_FINALS_MatchUp, SEMI_FINALS_TeamScoreList);


        //stage="FINAL" 필터링 - 결승
        List<Map<String, Object>> FINAL_matches = matches.stream()
                .filter(match -> "FINAL".equals(match.get("stage")))
                .collect(Collectors.toList());
        //16강 대진
        Map<Integer, Integer> FINAL_MatchUp = getMatchUp(FINAL_matches);
        //16강 1,2차전 누적 스코어
        Map<Integer, TeamScore> FINAL_TeamScoreList = getTeamScoreList(FINAL_matches);
        //16강 결과 추출
        List<StageScoreDto> FINAL = getResult(FINAL_MatchUp, FINAL_TeamScoreList);


        return new Result(LAST_16, QUARTER_FINALS, SEMI_FINALS, FINAL);
    }

    // stage 대진 추출
    private static Map<Integer, Integer> getMatchUp(List<Map<String, Object>> stageMatches) {
        // 1차전 매치만 추출
        List<Map<String, Object>> matchday_1 = stageMatches.stream()
                .filter(match -> Integer.valueOf(1).equals(match.get("matchday")))
                .collect(Collectors.toList());
        Map<Integer, Integer> matchUp = new HashMap<>(); // 16강 대진
        for (Map<String, Object> match : matchday_1) {
            Map<String, Object> homeTeam = (Map<String, Object>) match.get("homeTeam");
            Integer homeTeamId = (Integer) homeTeam.get("id");
            Map<String, Object> awayTeam = (Map<String, Object>) match.get("awayTeam");
            Integer awayTeamId = (Integer) awayTeam.get("id");
            matchUp.put(homeTeamId, awayTeamId);
        }

        return matchUp;
    }


    //stage 1,2차전 누적 스코어
    private static Map<Integer, TeamScore> getTeamScoreList(List<Map<String, Object>> matches) {
        Map<Integer, TeamScore> map = new HashMap<>();

        for (Map<String, Object> match : matches) {
            Map<String, Object> homeTeam = (Map<String, Object>) match.get("homeTeam");
            Integer homeTeamId = (Integer) homeTeam.get("id");
            String homeTeamName = (String) homeTeam.get("name");
            String homeTeamCrest = (String) homeTeam.get("crest");
            Map<String, Object> awayTeam = (Map<String, Object>) match.get("awayTeam");
            Integer awayTeamId = (Integer) awayTeam.get("id");
            String awayTeamName = (String) awayTeam.get("name");
            String awayTeamCrest = (String) awayTeam.get("crest");
            Map<String, Object> score = (Map<String, Object>) match.get("score");

            Map<String, Object> fullTime = (Map<String, Object>) score.get("fullTime");
            Integer homeFullTime = (Integer) fullTime.get("home");
            Integer awayFullTime = (Integer) fullTime.get("away");

            Integer homeTeamScore = 0;
            Integer awayTeamScore = 0;
            Integer homePen = 0;
            Integer awayPen = 0;
            //승부차기 까지 갔는지 확인
            String duration = (String) score.get("duration");
            if (duration.equals("PENALTY_SHOOTOUT")) { //PENALTY_SHOOTOUT

                Map<String, Object> penalties = (Map<String, Object>) score.get("penalties");
                homePen = (Integer) penalties.get("home");
                awayPen = (Integer) penalties.get("away");

                homeTeamScore = homeFullTime - homePen;
                awayTeamScore = awayFullTime - awayPen;
            } else { //REGULAR
                homeTeamScore = homeFullTime;
                awayTeamScore = awayFullTime;
            }

            TeamScore homeTeamInfo = new TeamScore(homeTeamId, homeTeamName, homeTeamCrest, homeTeamScore, homePen);
            TeamScore awayTeamInfo = new TeamScore(awayTeamId, awayTeamName, awayTeamCrest, awayTeamScore, awayPen);


            //1차전인 경우
            if (Integer.valueOf(1).equals(match.get("matchday"))) {
                homeTeamInfo.setFirstScore(homeFullTime);
                awayTeamInfo.setFirstScore(awayFullTime);
            } else { // 2차전인 경우
                homeTeamInfo.setSecondScore(homeTeamScore);
                awayTeamInfo.setSecondScore(awayTeamScore);
            }


            //1, 2차전 총 합산 스코어
            map.merge(homeTeamId, homeTeamInfo,
                    (existing, newScore) -> {
                        int updatedScore = Optional.ofNullable(existing.getTotalScore()).orElse(0)
                                + Optional.ofNullable(newScore.getTotalScore()).orElse(0);
                        int updatedPenalties = Optional.ofNullable(existing.getPenalties()).orElse(0)
                                + Optional.ofNullable(newScore.getPenalties()).orElse(0);

                        return new TeamScore(existing.getId(), existing.getTeamName(), existing.getTeamCrest(), updatedScore, updatedPenalties,
                                existing.getFirstScore(), newScore.getSecondScore());
                    });

            map.merge(awayTeamId, awayTeamInfo,
                    (existing, newScore) -> {
                        int updatedScore = Optional.ofNullable(existing.getTotalScore()).orElse(0)
                                + Optional.ofNullable(newScore.getTotalScore()).orElse(0);
                        int updatedPenalties = Optional.ofNullable(existing.getPenalties()).orElse(0)
                                + Optional.ofNullable(newScore.getPenalties()).orElse(0);
                        return new TeamScore(existing.getId(), existing.getTeamName(), existing.getTeamCrest(), updatedScore, updatedPenalties,
                                existing.getFirstScore(), newScore.getSecondScore());
                    });
        }
        return map;
    }

    //stage 1,2차전 누적 결과 추출
    private static List<StageScoreDto> getResult(
            Map<Integer, Integer> matchUp, Map<Integer, TeamScore> stageScore) {

        List<StageScoreDto> result = new ArrayList<>();
        for (Integer i : matchUp.keySet()) {
            //16강 대진
            TeamScore homeTeam = stageScore.get(i); //HomeTeam
            TeamScore awayTeam = stageScore.get(matchUp.get(i)); //AwayTeam
            StageScoreDto stageScoreDto = new StageScoreDto(homeTeam, awayTeam);
            result.add(stageScoreDto);
        }

        return result;
    }


    @Data
    @AllArgsConstructor
    private static class TeamScore {
        private Integer id;
        private String TeamName;
        private String TeamCrest;

        private Integer totalScore;
        private Integer Penalties;

        private Integer firstScore;
        private Integer secondScore;

        public TeamScore(Integer id, String teamName, String teamCrest, Integer totalScore, Integer penalties) {
            this.id = id;
            TeamName = teamName;
            TeamCrest = teamCrest;
            this.totalScore = totalScore;
            Penalties = penalties;
        }
    }

    @Data
    @AllArgsConstructor
    public static class StageScoreDto {
        private TeamScore homeTeamScore;
        private TeamScore awayTeamScore;
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data1;
        private T data2;
        private T data3;
        private T data4;
    }
}
