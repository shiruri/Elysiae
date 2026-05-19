package com.shiro.elysiae.util;

import com.shiro.elysiae.model.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {

    private final Key key;
    private final long jwtExpirationMs;

    public JwtUtils(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration-ms}") long jwtExpirationMs) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpirationMs = jwtExpirationMs;
    }

    // ----------------------------------------------------------------
    // Generate
    // ----------------------------------------------------------------

    public String generateToken(long userId, Role role, boolean mustChangePassword) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role.name())
                .claim("mustChangePassword", mustChangePassword)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ----------------------------------------------------------------
    // Extract
    // ----------------------------------------------------------------

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        String role = extractAllClaims(token).get("role", String.class);
        return Role.valueOf(role).name();
    }

    public boolean extractMustChangePassword(String token) {
        Boolean value = extractAllClaims(token).get("mustChangePassword", Boolean.class);
        return value != null && value;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    // ----------------------------------------------------------------
    // Validate
    // ----------------------------------------------------------------

    public boolean validateToken(String token, String userId) {
        try {
            String extractedUserId = extractUserId(token);
            return extractedUserId.equals(userId) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateToken(String token, long userId) {
        return validateToken(token, String.valueOf(userId));
    }

    // ----------------------------------------------------------------
    // Internal
    // ----------------------------------------------------------------

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}