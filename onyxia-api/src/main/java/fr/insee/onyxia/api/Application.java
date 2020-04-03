package fr.insee.onyxia.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "io.github.inseefrlab")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
