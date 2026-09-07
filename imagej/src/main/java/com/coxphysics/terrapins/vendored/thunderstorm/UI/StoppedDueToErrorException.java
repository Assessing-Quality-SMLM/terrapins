package com.coxphysics.terrapins.vendored.thunderstorm.UI;

public class StoppedDueToErrorException extends RuntimeException {
    
    public StoppedDueToErrorException() {
    }

    public StoppedDueToErrorException(String message) {
        super(message);
    }

    public StoppedDueToErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public StoppedDueToErrorException(Throwable cause) {
        super(cause);
    }

}
