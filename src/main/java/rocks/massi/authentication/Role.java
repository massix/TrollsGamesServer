package rocks.massi.authentication;

public enum Role {
    USER("user"),
    MASTER("master"),
    CRAWLER("crawler"),
    ROLE("developer");

    String value;

    Role(final String value) {
        this.value = value;
    }
}
