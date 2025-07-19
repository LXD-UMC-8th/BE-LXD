package org.lxdproject.lxd.apiPayload.code.exception.handler;

import org.lxdproject.lxd.apiPayload.code.status.BaseErrorCode;

public class MailHandler extends GeneralException{
    public MailHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
