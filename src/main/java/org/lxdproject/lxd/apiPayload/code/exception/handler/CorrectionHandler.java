package org.lxdproject.lxd.apiPayload.code.exception.handler;

import org.lxdproject.lxd.apiPayload.code.status.BaseErrorCode;

public class CorrectionHandler extends GeneralException {
    public CorrectionHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
