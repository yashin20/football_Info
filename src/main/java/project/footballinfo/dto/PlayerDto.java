package project.footballinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerDto {

    private Long playerId;
    private String name;
    private String position; //포지션
    private String dateOfBirth; //생년월일
    private String nationality; //국적

}
