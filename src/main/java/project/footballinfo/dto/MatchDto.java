package project.footballinfo.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchDto {

    private String matchDate; //매치 날짜
    private String matchEmblem; //매치 타입 로고(리그, 컵, 유럽대항전)
    private String status; //상태 (이미 끝난 매치인지 아닌지) -> 이걸로 스코어를 검색할지 말지
    private String homeTeamName; //홈팀 이름
    private String homeTeamCrest; //홈팀 마크
    private String awayTeamName; //원정팀 이름
    private String awayTeamCrest; //원정팀 마크
    private Integer homeTeamScore; //스코어 (홈팀)
    private Integer awayTeamScore; //스코어 (원정팀)
}
