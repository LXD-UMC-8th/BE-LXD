package org.lxdproject.lxd.apiPayload.code.exception.handler;

import org.lxdproject.lxd.apiPayload.code.status.BaseErrorCode;

public class StorageHandler extends GeneralException{
    public StorageHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
