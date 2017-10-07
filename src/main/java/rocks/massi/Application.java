package rocks.massi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import rocks.massi.connector.SQLiteConnector;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public SQLiteConnector makeConnector() {
        return new SQLiteConnector();
    }
}
