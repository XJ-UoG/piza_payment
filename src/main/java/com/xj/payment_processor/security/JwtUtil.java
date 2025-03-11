package com.xj.payment_processor.security;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Service;

import java.util.Date;

import javax.crypto.SecretKey;

@Service
public class JwtUtil {
    private String JWT_SECRET;
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;


    private SecretKey getSigningKey() {
        Dotenv dotenv = Dotenv.load();
        this.JWT_SECRET = dotenv.get("JWT_SECRET");
        byte[] keyBytes = JWT_SECRET.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username){
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(getSigningKey())
            .compact();
    }

    public boolean validateToken(String token, String username){
        return (username.equals(getUserName(token)) && !isTokenExpired(token));
    }

    public String getUserName(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public boolean isTokenExpired(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                  .parser()
                  .verifyWith(getSigningKey())
                  .build()
                  .parseSignedClaims(token)
                  .getPayload();
        }
}
