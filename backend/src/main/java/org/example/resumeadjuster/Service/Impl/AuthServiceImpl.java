package org.example.resumeadjuster.Service.Impl;




import com.google.api.client.json.webtoken.JsonWebToken;
import com.google.auth.oauth2.JwtProvider;
import org.example.resumeadjuster.Security.JwtTokenProvider;
import org.example.resumeadjuster.Model.DTO.AuthResponseDTO;
import org.example.resumeadjuster.Model.DTO.UserResponseDTO;
import org.example.resumeadjuster.Model.DTO.SignupRequestDTO;
import org.example.resumeadjuster.Model.DTO.LoginRequestDTO;

import org.example.resumeadjuster.Model.Entity.User;
import org.example.resumeadjuster.Model.Entity.UserAuthProvider;

import org.example.resumeadjuster.Repository.UserRepository;
import org.example.resumeadjuster.Repository.UserAuthProviderRepository;
import org.example.resumeadjuster.Service.AuthService;
import org.example.resumeadjuster.Service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// Google related library

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import java.util.Collections;


/*
Implementation of AuthService that provides authentication-related functionality.
including signup, login, firebase authentication ,token ,validation, and logout

This service handle:
1. User registration (sign up)
2. local authentication with email/password
3. OAuth authentication with Firebase (Google)
4. token validation
5. User logout

Redis is used for token storage and blacklisting, with configurable expiration times

 */

@Service
public class AuthServiceImpl implements AuthService {
    // Spring security's authentication manager for handling login credentials
    @Autowired
    private AuthenticationManager authenticationManager;

    // Repository for user data access
    @Autowired
    private UserRepository userRepository;

    // Repository for OAuth provider information
    @Autowired
    private UserAuthProviderRepository userAuthProviderRepository;


    // service for user-related operations
    @Autowired
    private UserService userService;
    // password encoded for secure storage of password

    // spring security configure bCryptPasswordEncoder
    @Autowired
    private PasswordEncoder passwordEncoder;

    //JWT token provider for creating and validation tokens
    @Autowired
    private JwtTokenProvider jwtTokenProvider;


/*
    Redis template for token blacklisting and validation
 */

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    //JWT token expiration time in milliseconds
    @Value("${jwt.expiration-ms:3600000}")
    private long jwtExpirationMs;

    // prefix for Redis token blacklist keys
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:token";

    // Register a new user with the application



    @Override
    @Transactional
    public AuthResponseDTO signup(SignupRequestDTO signupRequestDTO) {
        // check if email already exist
        if(userRepository.existsByEmail(signupRequestDTO.getEmail())) {
            throw new IllegalArgumentException("Email address already in use");
        }
        // create new user entity
        User user=new User();
        user.setFirstName(signupRequestDTO.getFirstName());
        user.setLastName(signupRequestDTO.getLastName());
        user.setEmail(signupRequestDTO.getEmail());
        user.setPasswordHash(passwordEncoder.encode(signupRequestDTO.getPassword()));
        user.setEmailVerified(false); // requires verification

        //Save user to database ， so now we have already get user id
        User savedUser=userService.createUser(user);

        // generate token  User email and ID
        String token=jwtTokenProvider.generateToken(savedUser.getEmail(),savedUser.getUserId());

    // Create response with token and user details
        AuthResponseDTO authResponseDTO=new AuthResponseDTO();
        authResponseDTO.setToken(token);
        authResponseDTO.setUser(userService.mapToDTO(savedUser));



        return authResponseDTO;
    }


//Authenticate user with email and password
    @Override
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // 手动比较密码
        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // update last login time
        userService.updateLastLogin(user);

        //Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getEmail(),user.getUserId());

        // Create response with token and user details
        AuthResponseDTO authResponse = new AuthResponseDTO();
        authResponse.setToken(token);
        authResponse.setUser(userService.mapToDTO(user));


        return authResponse;
    }


    /*
   Authenticate with Google via firebase
   This method is the core logic for handling Google authentication
   It achieves this by verifying a Firebase ID token provided by the client (frontend)
  and then either authenticates an existing user or creates a new user within the application's database.
   @param firebaseToken The Firebase ID token received from the frontend client. This token
 * is used to verify the user's identity with Firebase.

 * @return AuthResponseDTO An object containing the generated JWT token for the authenticated
 * user and the user's data (mapped to a DTO for security and data transfer).
 * @throws RuntimeException If any error occurs during the authentication process, such as
 * token verification failure, database errors, or other unexpected issues.

    */
    @Override
    @Transactional
    public AuthResponseDTO authenticateWithGoogle(String idToken) {
        try{
            /*
            step 1 verify the Firebase Token , use the Firebase Admin SDK to verify the integrity and validity of the
            provided Firebase id token,This is a crucial security step to ensure the user is who they claim to be.
             */
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            /*
            Step2. Extract User Information from the Decoded token

             */
            String uid=decodedToken.getUid(); //he unique user ID assigned by Firebase
            String email=decodedToken.getEmail(); // The user's email address
            boolean emailVerified=decodedToken.isEmailVerified(); // indicates if the user has verified their email


            /*
            Step3. Check if User exists in the database with the given Firebase UID.
            This allows us to handle returning users who have previously logged in with Google
             */
            Optional<UserAuthProvider> authProvider = userAuthProviderRepository.findByProviderAndProviderId("firebase", uid);
            User user;

            if(authProvider.isPresent()){
                // step 4a If the user exists (found by firebase uid), get the corresponding User object
                user=authProvider.get().getUser();
                UserAuthProvider existingProvider = authProvider.get();
                existingProvider.setAccessToken(idToken); // 保存idToken作为访问令牌
                userAuthProviderRepository.save(existingProvider);
            }else{
                //step 4b . if not , need to either:
                // Link an existing user with the Firebase Account
                // create a brand new user
                Optional<User> existingUser=userRepository.findByEmail(email);
                if(existingUser.isPresent()){
                    user=existingUser.get();
                    // Create a new UserAuthProvider entry to associate the user with Firebase.
                    UserAuthProvider firebaseAuthProvider = new UserAuthProvider();
                    firebaseAuthProvider.setUser(user);
                    firebaseAuthProvider.setProvider("firebase");
                    firebaseAuthProvider.setProviderId(uid);

                    firebaseAuthProvider.setAccessToken(idToken);
                    userAuthProviderRepository.save(firebaseAuthProvider); // save it
                }else{
                    /*
                    create new user

                     */
                    user=new User();
                    user.setEmail(email);
                    user.setEmailVerified(emailVerified);
                    // extract name information from claims
                    String displayName=decodedToken.getName();

                    //Split displayName into first and last name
                    if(displayName !=null && !displayName.isEmpty()){
                        String[] nameParts=displayName.split(" ",2);
                        user.setFirstName(nameParts[0]);
                        user.setLastName(nameParts.length > 1 ? nameParts[1] : "");

                    }else{
                        user.setFirstName("firebase User");
                        user.setLastName("");
                    }

                    // No password for OAuth users
                    user.setPasswordHash(null);

                    // Save user in database so it alreday has userId
                    user = userService.createUser(user);
                    //create auth provider entry
                    UserAuthProvider firebaseAuthProvider = new UserAuthProvider();
                    firebaseAuthProvider.setUser(user);
                    firebaseAuthProvider.setProvider("firebase");
                    firebaseAuthProvider.setProviderId(uid);
//为新用户设置访问令牌
                    firebaseAuthProvider.setAccessToken(idToken);

                    userAuthProviderRepository.save(firebaseAuthProvider);

                }
            }
            System.out.println("Successfully authenticated user with Google: " + email);

            // update last login
            userService.updateLastLogin(user);
            //generate JWT token
            String token=jwtTokenProvider.generateToken(user.getEmail(),user.getUserId());

            //create response
            AuthResponseDTO authResponse = new AuthResponseDTO();
            authResponse.setToken(token);
            authResponse.setUser(userService.mapToDTO(user));
            return authResponse;


        }catch (Exception e){
            System.err.println("Google authentication failed: " + e.getMessage());
            e.printStackTrace(); // 打印堆栈跟踪以便调试
            throw new RuntimeException("Error authenticating with Firebase : "+e.getMessage(),e);
        }

    }





    /*
     When a user logs out, their active JWT should no longer grant access to protected resources.
     Simply relying on the token's natural expiration might leave a window where the token could be
    intercepted and reused. Blacklisting provides an immediate way to revoke access.
    This method is responsible for invalidating a JWT

    By blacklisting the token, we prevent it from being used for future authenticated requests,
    even if it hasn't naturally expired yet.
     */
    @Override
    public void logout(String token) {
        //Step 1: clean the token (remove Bearer prefix if present)
        String cleanToken=token;
        if(token.startsWith("Bearer ")) {
            cleanToken=token.substring(7);
        }

        //Step 2: get token expiration time from JWT
        long expirationMs=jwtTokenProvider.getExpirationFromToken(cleanToken);
        long currentTimeMs=System.currentTimeMillis();
        long ttlMs=expirationMs-currentTimeMs;

        if(ttlMs>0){
            // Step 3: Add Token to Blacklist with Expiration (if it's not already expired)**
            // We only add the token to the blacklist if it has not already expired.
            String blacklistKey=TOKEN_BLACKLIST_PREFIX+cleanToken;
            // **Redis Operation:** We use the RedisTemplate to store the blacklisted token.
            // - The key is the unique blacklist key we constructed.
            // - The value is simply "true" (the presence of the key is what matters for blacklisting).
            // - The expiration time (`ttlMs`) is set to match the original remaining time-to-live of the JWT.
            //   This ensures that the blacklisted token is automatically removed from Redis once its
            //   natural expiration time is reached, preventing the blacklist from growing indefinitely
            //   with permanently expired tokens.
            redisTemplate.opsForValue().set(blacklistKey,"true",ttlMs, TimeUnit.MILLISECONDS);
        }

    }

    // check if a JWT token is valid
    @Override
    public boolean isValidToken(String token) {
        // clean the token (remove Bearer prefix if present)
        String cleanToken=token;
        if(token.startsWith("Bearer ")) {
            cleanToken=token.substring(7);
        }
        //Check if token is in blacklist
        String blacklistKey=TOKEN_BLACKLIST_PREFIX+cleanToken;
        Boolean isBlacklisted=redisTemplate.hasKey(blacklistKey);

        if(Boolean.TRUE.equals(isBlacklisted)){
            return false;
        }

       //validate token with provider

        return jwtTokenProvider.validateToken(cleanToken);
    }
}
