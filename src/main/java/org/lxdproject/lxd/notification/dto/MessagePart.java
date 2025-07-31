package org.lxdproject.lxd.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessagePart {
    private String type; // text 또는 bold
    private String value;
}
