package org.lxdproject.lxd.domain.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lxdproject.lxd.domain.diary.entity.enums.Language;

@Getter
@Setter
@NoArgsConstructor
public class LanguageSettingRequestDTO {
    private Language systemLanguage;
}

