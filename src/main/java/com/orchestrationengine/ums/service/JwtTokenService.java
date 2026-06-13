package com.orchestrationengine.ums.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * Service to handle JWT generation, verification, and decoding.
 */
@Service
public class JwtTokenService {

    private static final String SECRET = "RajOrchestrationEngineSecureKey123456789";
    private static final String ISSUER = "orchestration-engine";
    private final Algorithm algorithm = Algorithm.HMAC256(SECRET);

    // 15 minutes access token expiry
    private static final long ACCESS_TOKEN_EXPIRY_MS = 15 * 60 * 1000L;
    // 7 days refresh token expiry
    private static final long REFRESH_TOKEN_EXPIRY_MS = 7 * 24 * 60 * 60 * 1000L;

    public String generateAccessToken(UUID userProfileId, String username) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(userProfileId.toString())
                .withClaim("username", username)
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY_MS))
                .sign(algorithm);
    }

    public String generateRefreshToken(UUID userProfileId) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(userProfileId.toString())
                .withJWTId(UUID.randomUUID().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY_MS))
                .sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) {
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        return verifier.verify(token);
    }
}
