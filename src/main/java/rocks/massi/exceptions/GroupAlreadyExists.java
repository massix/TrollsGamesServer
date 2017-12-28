package rocks.massi.exceptions;

public class GroupAlreadyExists extends RuntimeException {
    public GroupAlreadyExists(String message) {
        super(message);
    }
}
