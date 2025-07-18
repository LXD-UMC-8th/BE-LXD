package org.lxdproject.lxd.diary.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lxdproject.lxd.diary.entity.enums.Language;

@Getter
@NoArgsConstructor
public class QuestionRequestDTO {
    private Language language;
}
