package me.folf.podns4j;

/**
 * Exception thrown when a pronoun record cannot be parsed.
 */
public class PronounParseException extends Exception {
    /**
     * Creates exception with message.
     * 
     * @param message the error message
     */
    public PronounParseException(String message) {
        super(message);
    }

    /**
     * Creates exception with message and cause.
     * 
     * @param message the error message
     * @param cause   the cause
     */
    public PronounParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
