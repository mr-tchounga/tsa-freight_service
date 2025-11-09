package com.shifter.freight_service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Launcher {

//    private static final Logger logger = LoggerFactory.getLogger(App.class);
    public static void main(String[] args) {

         Dotenv dotenv = Dotenv.load();
         dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        SpringApplication.run(com.shifter.freight_service.Launcher.class, args);
//        loggerr("Server is running on " + System.getProperty("server.port"));
        System.out.println("Server is running...");

    }

}

