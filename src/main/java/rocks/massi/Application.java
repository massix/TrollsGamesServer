package rocks.massi;

import feign.Feign;
import feign.jaxb.JAXBContextFactory;
import feign.jaxb.JAXBDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import rocks.massi.services.BoardGameGeek;

import java.util.Properties;

@SpringBootApplication
public class Application extends SpringApplication {

    @Bean
    public BCryptPasswordEncoder makeEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public BoardGameGeek boardGameGeek(@Value("${bgg.url}") String bggUrl) {
        JAXBContextFactory contextFactory = new JAXBContextFactory.Builder().build();
        return Feign.builder().decoder(new JAXBDecoder(contextFactory)).target(BoardGameGeek.class, bggUrl);
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
