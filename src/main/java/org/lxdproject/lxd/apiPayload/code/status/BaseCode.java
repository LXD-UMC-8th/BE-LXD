package org.lxdproject.lxd.apiPayload.code.status;

import org.lxdproject.lxd.apiPayload.code.dto.ReasonDTO;

public interface BaseCode {

    ReasonDTO getReason();
    ReasonDTO getReasonHttpStatus();
}
