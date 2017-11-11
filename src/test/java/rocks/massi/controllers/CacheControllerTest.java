package rocks.massi.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import rocks.massi.controllers.utils.AuthorizationHandler;
import rocks.massi.data.CacheOperation;

import static org.junit.Assert.assertTrue;

@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CacheControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        AuthorizationHandler.setUp(restTemplate, "test@example.com", "user");
    }

    @Test
    public void purgeCache() {
        ResponseEntity<Void> responseEntity = restTemplate.exchange("/v1/cache/purge", HttpMethod.DELETE, null, Void.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void purgeExpired() {
        restTemplate.delete("/v1/cache/expired");
    }

    @Test
    public void getMemoryCache() throws Exception {
        ResponseEntity<CacheOperation> responseEntity = restTemplate.getForEntity("/v1/cache/get", CacheOperation.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertTrue(responseEntity.getBody().isSuccess());
    }

}