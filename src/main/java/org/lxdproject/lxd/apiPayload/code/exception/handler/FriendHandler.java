package org.lxdproject.lxd.apiPayload.code.exception.handler;

import org.lxdproject.lxd.apiPayload.code.status.BaseErrorCode;

public class FriendHandler extends GeneralException {
    public FriendHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
