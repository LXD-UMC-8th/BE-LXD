package org.lxdproject.lxd.global.apiPayload.code.exception.handler;

import org.lxdproject.lxd.global.apiPayload.code.status.BaseErrorCode;

public class MailHandler extends GeneralException{
    public MailHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
