package org.example.resumeadjuster.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/*
JWT token Provider
This component handler all JWT (JSON WEB TOKEN) related operations including:
1.Token generation
2.Token validation
3.Extracting claims from tokens
4.Retrieving token expiration time

Jwt tokens are used to maintain stateless authentication in the application.
They contain encoded user information and are signed to ensure data integrity
 */




@Component
public class JwtTokenProvider {
/*
Secret key used for signing JWT token (for user)
Loaded from application properties and should be kept secure
This value should be least 256 bits (32 chracters) for HS256 algorithm
 */
@Value("${jwt.secret}")
    private String jwtSecret;
    /*
    Token expiration time in milliseconds
    Default is 1 hours(3600000ms) if not specified in properties
    can be configured via application.properties or environment variables

     */
    @Value("${jwt.expiration-ms:3600000}")
    private long jwtExpirationMs;


    /*
    Generates a new JWT token for a user
    The token includes:
    -Subject claim (user's email)
    -Issued at timestamp
    -  Expiration timestamp
    - Crpytographic signature using the secret key
     */


    public String generateToken(String email, Long userId) {
        // get current timestap for token issuance
        Date now = new Date();
        //calculate expiration timestamp based on configured duration
        Date expirationDate = new Date(now.getTime() + jwtExpirationMs);

        // create a cryptographic key from the JWT secret
        // this key is used to sign the token , ensuring it cannot be tampered with
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        //build the JWT token with all required claim and signature
        return Jwts.builder()
                .setSubject(email) // set user identifier (email)
                .claim("userId",userId) // and user Id as a custom claim
                .setIssuedAt(now) // record token creation time
                .setExpiration(expirationDate) // set token expiration time
                .signWith(key) // sign token with the secret key
                .compact(); //serialize token to compact form


    }


    /*
    Validates a JWT token's signature and expiration

    This method checks if:
    - The token has a valid signature (has not been tampered with)
    - the token has not expired
    - the token is well-formed and can be parsed

     */

    public boolean validateToken(String token) {
        try{
            //create key from secret for signature verification
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            //Attempt to parse and validate the token
            // This will verify the signature and check expiration
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            // if no exception were thrown, token is valid
            return true;
        }catch(Exception e){
            // Token is invalid if any exception occurs during validation
            // This includes signature failures, expiration, and malformed tokens
            return false;
        }
    }
    /*
    Extracts the user's email from a JWT token
    This method retrieves the subject claim from the token
    which contains the user's email address
     */
    public String getEmailFromToken(String token){
        //create key from secret for token verification
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        //parse token and extract all claims
        Claims claims=Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        //return the subject claim, which contains the user's email
        return claims.getSubject();
    }
    // extract the user's id from a JWT token
// This method retrieves the userId custom claim from the token.
    public Long getUserIdFromToken(String token) {
        // Create key from secret for token verification
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        // Parse token and extract all claims
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Return the userId claim
        return claims.get("userId", Long.class);
    }

    /*
    extract the expiration timestamp from a JWT token
    this method extracts the expiration time claim from the token
     and returns it as milliseconds since epoch.
     */
    public long getExpirationFromToken(String token) {
        // Create key from secret for token verification
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        // Parse token and extract all claims
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Return the expiration timestamp in milliseconds
        return claims.getExpiration().getTime();
    }
}

