package project.footballinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class StandingsDto {

    private Integer teamId;
    private Integer position; //순위
    private String crest; //로고
    private String teamName; //팀명
    private Integer playedGames; //경기수
    private Integer points; //승점
    private Integer won; //승
    private Integer draw; //무
    private Integer lost; //패
    private Integer goalsFor; //득점
    private Integer goalsAgainst; //실점
    private Integer goalDifference; //득실차
}
