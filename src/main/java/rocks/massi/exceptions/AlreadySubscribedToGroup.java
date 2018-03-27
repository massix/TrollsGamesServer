package rocks.massi.exceptions;

public class AlreadySubscribedToGroup extends RuntimeException {
    public AlreadySubscribedToGroup(String message) {
        super(message);
    }
}
