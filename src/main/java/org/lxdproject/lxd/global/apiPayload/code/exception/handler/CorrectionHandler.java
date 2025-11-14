package org.lxdproject.lxd.global.apiPayload.code.exception.handler;

import org.lxdproject.lxd.global.apiPayload.code.status.BaseErrorCode;

public class CorrectionHandler extends GeneralException {
    public CorrectionHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
