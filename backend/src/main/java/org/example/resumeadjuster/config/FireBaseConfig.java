package org.example.resumeadjuster.config;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;;
import java.io.IOException;
import java.io.FileInputStream;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Configuration class for Firebase initialization
 * This class loads Firebase credentials and initializes the Firebase SDK
 * for authentication and other Firebase services
 */


@Configuration
public class FireBaseConfig {

    private static final Logger logger = Logger.getLogger(FireBaseConfig.class.getName());
    /*
    Path to the firebase service account credential JSON file
    save in resource folder named firebase-credentials
     */
    @Value("${firebase.credentials.path}")
    private String firebaseCredentialsPath;

    // Firebase projectId (optional )
    @Value("${firebase.project.id:#{null}}")
    private String projectId;

    @Autowired
    private ResourceLoader resourceLoader;

    /*
    Initialize Firebase SDK on application startup
    this method is called automatically after dependency injection is done
    it checks if Firebase is already initialized and if not, intializes it with the provider
     */
    @PostConstruct
    public void initialize() throws IOException {
        try{
        // check if firebase is already initialized
            List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
            if(firebaseApps==null||firebaseApps.isEmpty()){
                logger.info("Initializing Firebase App");
                // Load credentials file from resources
                Resource resource = resourceLoader.getResource(firebaseCredentialsPath);
                InputStream serviceAccount = resource.getInputStream();

                // Build Firebase options
                FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount));

                // Set project ID if provided
                if (projectId != null && !projectId.isEmpty()) {
                    optionsBuilder.setProjectId(projectId);
                }

                // Initialize Firebase
                FirebaseApp.initializeApp(optionsBuilder.build());
                logger.info("Firebase application has been initialized");
            }
                else{
                 logger.info("Firebase application is already initialized");
            }
        }catch(IOException e){
            logger.log(Level.SEVERE,"Error initializing Firebase application",e);
            throw e;
        }

    }

}