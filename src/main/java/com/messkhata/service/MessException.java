package com.messkhata.service;

/**
 * Thrown when the user asks for something the mess rules do not allow.
 * The message is shown to the user as it is.
 */
public class MessException extends Exception {

    public MessException(String message) {
        super(message);
    }
}
