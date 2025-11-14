package org.lxdproject.lxd.global.apiPayload.code.exception.handler;

import org.lxdproject.lxd.global.apiPayload.code.status.BaseErrorCode;

public class NotificationHandler extends GeneralException {
    public NotificationHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
