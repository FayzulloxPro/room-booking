package dev.fayzullokh.roombooking.exceptions;

public class CustomIllegalArgumentException extends RuntimeException {

    public CustomIllegalArgumentException() {
        super();
    }

    public CustomIllegalArgumentException(String message) {
        super(message);
    }
}
