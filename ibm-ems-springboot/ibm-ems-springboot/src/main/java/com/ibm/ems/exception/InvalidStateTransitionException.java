package com.ibm.ems.exception;

public class InvalidStateTransitionException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public InvalidStateTransitionException(String message) { super(message); }
}
