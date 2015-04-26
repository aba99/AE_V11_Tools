package com.automic.api;

/**
 * Exception which is thrown when the login process to the AE was not successful
 */
@SuppressWarnings("serial")
class LoginFailedException extends AeException {

    public LoginFailedException(String message) {
        super(message);
    }
}
