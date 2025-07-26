package org.lxdproject.lxd.apiPayload.code.exception.handler;

import org.lxdproject.lxd.apiPayload.code.status.BaseErrorCode;

public class AuthHandler extends GeneralException{
    public AuthHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
