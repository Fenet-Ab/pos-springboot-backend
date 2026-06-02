package com.pos.app.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String msg) { super(msg); }
}
