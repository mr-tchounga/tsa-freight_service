package com.shifter.freight_service.configs;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    public Dotenv dotenv() {
        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .load();

        String apiKey = dotenv.get("OPENAI_API_KEY");
        logger.info("Loaded OpenAI API Key: {}", apiKey != null ? "FOUND" : "NOT FOUND");

        // Log DB variables
         String dbUrl = dotenv.get("DB_URL_FREIGHT");
         String dbUsername = dotenv.get("DB_USERNAME");
         String dbPassword = dotenv.get("DB_PASSWORD");
//        String dbUrl = "jdbc:postgresql://localhost:5432/tsa_freight_db";
//        String dbUsername = "shifter";
//        String dbPassword = "sh!fter";
        logger.info("DB_URL_FREIGHT: {}", dbUrl);
        logger.info("DB_USERNAME: {}", dbUsername);
        logger.info("DB_PASSWORD: {}", dbPassword != null ? "FOUND" : "NOT FOUND");

        // Add to System properties
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
            logger.info("Set system property: {} = {}", entry.getKey(), entry.getValue());
        });

        return dotenv;
    }

}
