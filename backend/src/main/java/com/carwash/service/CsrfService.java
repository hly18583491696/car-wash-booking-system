package com.carwash.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class CsrfService {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(Long userId, long ttlSeconds) {
        long expires = Instant.now().getEpochSecond() + ttlSeconds;
        String nonce = UUID.randomUUID().toString().replaceAll("-", "");
        String payload = userId + ":" + expires + ":" + nonce;
        String signature = sign(payload);
        String token = payload + ":" + signature;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    public boolean validate(String token, Long userId) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length != 4) return false;
            Long uid = Long.parseLong(parts[0]);
            long expires = Long.parseLong(parts[1]);
            String nonce = parts[2];
            String sig = parts[3];
            if (!uid.equals(userId)) return false;
            if (Instant.now().getEpochSecond() > expires) return false;
            String payload = uid + ":" + expires + ":" + nonce;
            String expected = sign(payload);
            return expected.equals(sig);
        } catch (Exception e) {
            return false;
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] h = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(h);
        } catch (Exception e) {
            return "";
        }
    }
}