package project.footballinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UEFA_MatchDto {

    private String stage;
    private Integer matchDay;
    private String homeTeamCrest;
    private String homeTeamName;
    private String awayTeamCrest;
    private String awayTeamName;

    private Score fullTime;
    private Score halfTime;
    private Score extraTime;
    private Score penalties;
}

