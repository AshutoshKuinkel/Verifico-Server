package com.verifico.server.auth.JWT;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

@Service
public class JWTService {

  // set this value, on my pc
  @Value("${JWT_SECRET}")
  private String jwtSecret;

  @Value("${JWT_EXPIRY}")
  private int accessTokenMins;

  private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateAccessToken(Long userId, String username) {
    return Jwts.builder()
        .subject(userId.toString())
        .claim("username", username)
        .issuedAt(new Date())
        .expiration(Date.from(Instant.now().plusSeconds(accessTokenMins * 60)))
        .signWith(getSigningKey())
        .compact();
  }

  public Claims validateAccessToken(String token) {
    try {
      return Jwts.parser()
          .verifyWith(getSigningKey())
          .build()
          .parseClaimsJws(token)
          .getPayload();
    } catch (SignatureException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT signature");
    } catch (MalformedJwtException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT token");
    } catch (ExpiredJwtException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT token expired");
    } catch (UnsupportedJwtException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT token unsupported");
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT claims empty");
    }
  }

  public Long getUserIdFromToken(String token) {
    Claims claims = validateAccessToken(token);
    return Long.parseLong(claims.getSubject());
  }

  public String getUsernameFromToken(String token) {
    Claims claims = validateAccessToken(token);
    return claims.get("username", String.class);
  }
}
