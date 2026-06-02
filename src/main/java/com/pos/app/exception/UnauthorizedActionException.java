package com.pos.app.exception;
public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String msg) { super(msg); }
}