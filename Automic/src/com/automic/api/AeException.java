package com.automic.api;


/**
 * Exception which is thrown as soon there is an error
 * during an {@link com.uc4.communication.requests.XMLRequest}
 * communication with the Automation Engine
 * @see com.uc4.communication.Connection
 */
@SuppressWarnings("serial")
public class AeException extends RuntimeException {

    public AeException(String message, Exception cause) {
        super(message, cause);
    }

    public AeException(String message) {
        super(message);
    }
}
