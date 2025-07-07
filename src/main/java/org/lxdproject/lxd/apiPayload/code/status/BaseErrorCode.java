package org.lxdproject.lxd.apiPayload.code.status;

import org.lxdproject.lxd.apiPayload.code.dto.ErrorReasonDTO;

public interface BaseErrorCode {

    ErrorReasonDTO getReason();
    ErrorReasonDTO getReasonHttpStatus();
}
