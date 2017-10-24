package rocks.massi.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Properties;

@RestController
@RequestMapping("/v1/server")
public class ServerController {

    @GetMapping("/version")
    public String getServerVersion() throws IOException {
        final Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
        return properties.getProperty("server_version");
    }

    @GetMapping("/artifact")
    public String getServerArtifact() throws IOException {
        final Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
        return properties.getProperty("artifact_name");
    }
}
