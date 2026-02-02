package com.sachet.parallel_asynchronous.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);

    @Value("${product.config.secret_key}")
    private String SECRET_KEY;
    @Value("${product.config.token_expiration}")
    private long tokenExpiration;
    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    private boolean extractUserNameExpiration(String userName, String token) {
        LOGGER.info("The token is:{}", token);
        Claims claims = extractAllClaims(token);
        String email = resolve(claims, Claims::getSubject);
        Date exp = resolve(claims, Claims::getExpiration);
        LOGGER.info("Email {}, UserName {}, expirationDate{}", email, userName, exp);
        return email.equals(userName) && exp.compareTo(new Date()) > 0;
    }

    public <T> T resolve(Claims claims, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean validateToken(String userName, String token) {
        try {
            return extractUserNameExpiration(userName, token);
        } catch (Exception e) {
            LOGGER.error("Error while validating token {}", e.getMessage());
        }
        return false;
    }

}
