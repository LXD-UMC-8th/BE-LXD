package org.lxdproject.lxd.apiPayload.code.exception.handler;

import org.lxdproject.lxd.apiPayload.code.status.BaseErrorCode;

public class InvalidPageException extends GeneralException {

    public InvalidPageException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}

