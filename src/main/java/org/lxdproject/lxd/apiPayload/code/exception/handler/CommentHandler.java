package org.lxdproject.lxd.apiPayload.code.exception.handler;

import org.lxdproject.lxd.apiPayload.code.status.BaseErrorCode;

public class CommentHandler extends RuntimeException {
    public CommentHandler(String message) {
        super(message);
    }
}
