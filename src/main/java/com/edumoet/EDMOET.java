package com.edumoet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EDMOET {

    public static void main(String[] args) {
        SpringApplication.run(EDMOET.class, args);
    }
}

