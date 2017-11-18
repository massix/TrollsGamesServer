package rocks.massi.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginInformation {
    private final String email;
    private final String password;
}
