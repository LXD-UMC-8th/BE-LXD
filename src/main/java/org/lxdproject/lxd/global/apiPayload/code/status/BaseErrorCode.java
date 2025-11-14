package org.lxdproject.lxd.global.apiPayload.code.status;

import org.lxdproject.lxd.global.apiPayload.code.dto.ErrorReasonDTO;

public interface BaseErrorCode {

    ErrorReasonDTO getReason();
    ErrorReasonDTO getReasonHttpStatus();
}
