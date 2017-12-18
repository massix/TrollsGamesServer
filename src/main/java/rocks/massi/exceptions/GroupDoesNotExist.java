package rocks.massi.exceptions;

public class GroupDoesNotExist extends RuntimeException {
    public GroupDoesNotExist(String message) {
        super(message);
    }
}
