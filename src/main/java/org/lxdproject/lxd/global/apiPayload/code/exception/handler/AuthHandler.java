package org.lxdproject.lxd.global.apiPayload.code.exception.handler;

import org.lxdproject.lxd.global.apiPayload.code.status.BaseErrorCode;

public class AuthHandler extends GeneralException{
    public AuthHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
