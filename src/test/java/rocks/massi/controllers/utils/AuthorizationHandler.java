package rocks.massi.controllers.utils;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import rocks.massi.data.LoginInformation;
import rocks.massi.data.User;

import java.util.Collections;

import static org.junit.Assert.assertTrue;

public class AuthorizationHandler {
    public static void setUp(TestRestTemplate restTemplate) {
        LoginInformation loginInformation = new LoginInformation("massi@massi.rocks", "supersecret");
        final ResponseEntity<User> authorizedUser = restTemplate.postForEntity("/v1/users/login", loginInformation, User.class);
        assertTrue(authorizedUser.getStatusCode().is2xxSuccessful());

        restTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("Authorization", authorizedUser.getHeaders().get("Authorization").get(0));
                    return execution.execute(request, body);
                }));
    }

    public static void setUpNormalUser(TestRestTemplate restTemplate, LoginInformation loginInformation) {
        final ResponseEntity<User> authorizedUser = restTemplate.postForEntity("/v1/users/login", loginInformation, User.class);
        assertTrue(authorizedUser.getStatusCode().is2xxSuccessful());

        restTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("Authorization", authorizedUser.getHeaders().get("Authorization").get(0));
                    return execution.execute(request, body);
                }));
    }
}
