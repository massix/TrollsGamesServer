package rocks.massi.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rocks.massi.data.ServerInformation;

import java.io.IOException;
import java.util.Properties;

@RestController
@RequestMapping("/v1/server")
public class ServerController {

    @CrossOrigin
    @GetMapping("/information")
    public ServerInformation getServerInformation() throws IOException {
        final Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
        return new ServerInformation(
                properties.getProperty("server_version"),
                properties.getProperty("artifact_name"),
                properties.getProperty("build_time"));
    }
}
