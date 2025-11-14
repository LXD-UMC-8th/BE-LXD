package org.lxdproject.lxd.global.apiPayload.code.exception.handler;

import org.lxdproject.lxd.global.apiPayload.code.status.BaseErrorCode;

public class MemberHandler extends GeneralException {
    public MemberHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
