package org.lxdproject.lxd.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lxdproject.lxd.diary.entity.enums.Language;

@Getter
@Setter
@NoArgsConstructor
public class LanguageSettingRequestDTO {
    private Language systemLanguage;
}

