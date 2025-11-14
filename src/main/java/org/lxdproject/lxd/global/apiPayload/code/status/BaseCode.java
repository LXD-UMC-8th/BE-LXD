package org.lxdproject.lxd.global.apiPayload.code.status;

import org.lxdproject.lxd.global.apiPayload.code.dto.ReasonDTO;

public interface BaseCode {

    ReasonDTO getReason();
    ReasonDTO getReasonHttpStatus();
}
