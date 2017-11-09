package rocks.massi.authentication;

public enum AuthenticationType {
    JWT("jwt"),
    NONE("none"),
    PASSWORD("password");

    String value;

    AuthenticationType(final String value) {
        this.value = value;
    }
}
