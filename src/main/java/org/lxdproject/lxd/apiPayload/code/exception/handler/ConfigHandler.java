package org.lxdproject.lxd.apiPayload.code.exception.handler;

import org.lxdproject.lxd.apiPayload.code.status.BaseErrorCode;

public class ConfigHandler extends GeneralException {
    public ConfigHandler(BaseErrorCode errorCode) {
      super(errorCode);
    }
}
