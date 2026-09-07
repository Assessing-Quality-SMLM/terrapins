package com.coxphysics.terrapins.vendored.thunderstorm.UI;

public class StoppedByUserException extends RuntimeException {

    public StoppedByUserException() {
    }

    public StoppedByUserException(String message) {
        super(message);
    }

    public StoppedByUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public StoppedByUserException(Throwable cause) {
        super(cause);
    }
}
