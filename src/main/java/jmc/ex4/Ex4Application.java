package jmc.ex4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableJpaAuditing // Enable JPA auditing for automatic timestamping of entities
@EnableScheduling // Enable scheduling for scheduled tasks
public class Ex4Application {

    public static void main(String[] args) {SpringApplication.run(Ex4Application.class, args);}

}
