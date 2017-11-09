package rocks.massi.exceptions;

public class WrongAuthenticationException extends RuntimeException {
    public WrongAuthenticationException(String message) {
        super(message);
    }
}
