package org.lxdproject.lxd.global.apiPayload.code.exception.handler;

import org.lxdproject.lxd.global.apiPayload.code.status.BaseErrorCode;

public class StorageHandler extends GeneralException{
    public StorageHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
