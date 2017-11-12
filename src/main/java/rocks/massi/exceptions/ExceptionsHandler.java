package rocks.massi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@SuppressWarnings("unused")
@ControllerAdvice
public class ExceptionsHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {
            UserNotFoundException.class,
            GameNotFoundException.class
    })
    protected ResponseEntity<Object> handleResourceNotFound(RuntimeException exception, WebRequest request) {
        return handleExceptionInternal(exception, null, null, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = {
            MalformattedUserException.class,
            MalformattedGameException.class
    })
    protected ResponseEntity<Object> handleMalformattedUser(RuntimeException exception, WebRequest request) {
        return handleExceptionInternal(exception, null, null, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = {
            AuthenticationException.class
    })
    protected ResponseEntity<Object> handleWrongAuthentication(RuntimeException exception, WebRequest request) {
        return handleExceptionInternal(exception, exception.getMessage(), null, HttpStatus.FORBIDDEN, request);
    }
}
