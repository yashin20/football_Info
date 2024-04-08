package project.footballinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScorersDto {

    private Integer position; //순위
    private String name; //이름
    private String crest; //로고
    private String teamName; //소속팀
    private Integer goals; //득점수
    private Integer assist; //도움수
    private Integer penalties; //패널티수
}
