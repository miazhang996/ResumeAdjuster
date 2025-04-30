package org.example.resumeadjuster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class ResumeAdjusterApplication {

    public static void main(String[] args) {
        // Load environment variables from .env file
        Dotenv dotenv = Dotenv.configure().load();

        // Set JWT_SECRET as a system property
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        SpringApplication.run(ResumeAdjusterApplication.class, args);
    }

}
