package project.footballinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Score {
    private Integer home;
    private Integer away;
}
