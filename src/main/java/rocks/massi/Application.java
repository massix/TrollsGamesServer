package rocks.massi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Properties;

@SpringBootApplication
@EnableAutoConfiguration
public class Application {

    @Bean
    public BCryptPasswordEncoder makeEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("mail.massi.rocks");
        javaMailSender.setUsername("noreply@massi.rocks");
        javaMailSender.setPassword("noreply");
        Properties props = javaMailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        javaMailSender.setPort(587);

        return javaMailSender;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
