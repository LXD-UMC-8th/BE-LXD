package org.lxdproject.lxd.apiPayload.code.exception.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lxdproject.lxd.apiPayload.code.dto.ErrorReasonDTO;
import org.lxdproject.lxd.apiPayload.code.status.BaseErrorCode;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

    private BaseErrorCode code;

    public ErrorReasonDTO getErrorReason() {
        return this.code.getReason();
    }

    public ErrorReasonDTO getErrorReasonHttpStatus(){
        return this.code.getReasonHttpStatus();
    }
}


