package project.footballinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SquadDto {

    //1. teamInfo - 팀 엠블럼, 팀명
    private String crest;
    private String teamName;

    //2. teamInfo - 참가중인 대회 엠블럼
    private List<String> emblem;

    //3. team squad - 코치 (이름, 생년월일, 국적)
    private String coachName;
    private String coachDateOfBirth;
    private String coachNationality;

    //4. team squad - 선수 명단 (name, position, dateOfBirth, nationality)
    private List<PlayerDto> playerDtos;

}
