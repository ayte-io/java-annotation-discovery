package io.ayte.utility.discovery.api.v1;

/**
 * Thrown in case compilation-time annotation processing stage hasn't been
 * run. Quite self-explanatory, innit?
 */
public class AbsentProcessingException extends RuntimeException {
    public AbsentProcessingException(String message) {
        super(message);
    }
}
