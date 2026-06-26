package org.example.templatejava6;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TemplateJava6Application {

    public static void main(String[] args) {
        SpringApplication.run(TemplateJava6Application.class, args);
    }

}
