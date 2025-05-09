package org.example.resumeadjuster.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS Configuration for the application
 * This configuration provides different settings for development and production environments
 */
@Configuration
public class CorsConfig {

    private final Environment environment;

    /**
     * Constructor injection of the Spring Environment
     * @param environment Spring Environment to detect active profiles
     */
    public CorsConfig(Environment environment) {
        this.environment = environment;
    }

    /**
     * Configures CORS settings based on the active profile
     * @return WebMvcConfigurer with appropriate CORS settings
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Check if we're in a development environment
                boolean isDev = isDevelopmentEnvironment();

                if (isDev) {
                    // Development environment - relaxed configuration
                    registry.addMapping("/**")             // Apply to all paths
                            .allowedOriginPatterns("*")    // Allow requests from any origin in dev
                            .allowedMethods("*")           // Allow all HTTP methods
                            .allowedHeaders("*")           // Allow all headers
                            .exposedHeaders("Authorization") // Make Authorization header visible to clients
                            .allowCredentials(true)        // Allow cookies and authentication
                            .maxAge(3600);                 // Cache preflight results for 1 hour

                    System.out.println("CORS configured for DEVELOPMENT environment");
                } else {
                    // Production environment - strict configuration
                    registry.addMapping("/**")
                            .allowedOrigins(
                                    "https://your-production-domain.com",
                                    "https://www.your-production-domain.com"
                            )                              // Only allow specific domains
                            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                            .allowedHeaders("Authorization", "Content-Type", "Accept")
                            .exposedHeaders("Authorization")
                            .allowCredentials(true)
                            .maxAge(3600);

                    System.out.println("CORS configured for PRODUCTION environment");
                }
            }
        };
    }

    /**
     * Determines if the application is running in a development environment
     * @return true if in development environment, false otherwise
     */
    private boolean isDevelopmentEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();

        // If no profiles are active, assume development
        if (activeProfiles.length == 0) {
            return true;
        }

        // Check for development profiles
        for (String profile : activeProfiles) {
            if ("dev".equals(profile) || "local".equals(profile) || "development".equals(profile)) {
                return true;
            }
        }

        return false;
    }
}