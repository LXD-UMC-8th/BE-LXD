package org.lxdproject.lxd.global.apiPayload.code.exception.handler;

import org.lxdproject.lxd.global.apiPayload.code.status.BaseErrorCode;

public class DiaryHandler extends GeneralException {
    public DiaryHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
