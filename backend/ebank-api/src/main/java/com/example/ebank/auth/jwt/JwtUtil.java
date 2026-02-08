package com.example.ebank.auth.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    // テスト用。実運用なら環境変数/設定ファイルに逃がすこと。
    private static final String SECRET =
            "change-this-secret-key-to-something-long-1234567890";
    private static final long EXPIRATION_MS = 60 * 60 * 1000; // 1時間

    private final Key key;

    public JwtUtil() {
        // 文字コードを固定（環境差でバイト列が変わる事故を防ぐ）
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * userId + username だけ入れる簡易版（roles無し）
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(username)      // sub
                .claim("id", userId)       // userId
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 互換用：旧呼び出しが残ってても動くようにする（rolesは無視）
     */
    public String generateToken(String username) {
        return generateToken(null, username);
    }

    // --- 抽出系 ---

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object idObj = claims.get("id");
        if (idObj == null) return null;

        // jjwtの実装やJSONパーサ都合で型が揺れるので吸収
        if (idObj instanceof Integer i) return i.longValue();
        if (idObj instanceof Long l) return l;
        if (idObj instanceof Number n) return n.longValue();
        return Long.valueOf(String.valueOf(idObj));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * validate: 署名OK & exp未期限切れ なら true
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
