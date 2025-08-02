package org.lxdproject.lxd.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.lxdproject.lxd.diary.entity.enums.Language;

@Getter
@Builder
@AllArgsConstructor
public class LanguageSettingResponseDTO {
    private Language nativeLanguage;
    private Language studyLanguage;
    private Language systemLanguage;
}

