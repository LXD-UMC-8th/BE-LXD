package org.lxdproject.lxd.apiPayload.code.exception.handler;

import org.lxdproject.lxd.apiPayload.code.status.BaseErrorCode;

public class DiaryHandler extends GeneralException {
    public DiaryHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
