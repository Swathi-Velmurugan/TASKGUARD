package com.example.taskguard.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {
    private static final String SECRET = "mySecretKeymySecretKeymySecretKey1234";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    //Generate JWT token
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role",userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }

    //Create JWT
    private String createToken(Map<String,Object> claims, String username){
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(KEY)
                .compact();

    }

    //Extract username
    public String extractUsername(String token){
        return extractClaim(token,Claims::getSubject);
    }

    //Extract generic claim
    public <T> T extractClaim(String token, Function<Claims,T> resolver){
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }
    
    //Extract all claims
    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //Extract expiration date from token
    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    //Token expiration check
    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    //Validate token
    public boolean validateToken(String token, UserDetails userDetails) {
        String username=extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

}