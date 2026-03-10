package com.luofeng.facetest.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Map;

public class JwtUtils {
    private static final String signKey = "faceTest"; // 签名密钥
    private static final Long expire = 86400000L; // 24小时过期时间(毫秒)
    
    /**
     * 生成JWT令牌
     * @param claims JWT第二部分负载(payload)中存储的内容
     * @return JWT令牌
     */
    public static String genToken(Map<String, Object> claims) {
        return Jwts.builder()
                .addClaims(claims) // 自定义信息（载荷）
                .signWith(SignatureAlgorithm.HS256, signKey) // 签名算法和密钥
                .setExpiration(new Date(System.currentTimeMillis() + expire)) // 过期时间
                .compact();
    }
    
    /**
     * 解析JWT令牌
     * @param jwt JWT令牌
     * @return JWT第二部分负载(payload)中存储的内容
     */
    public static Claims parseJWT(String jwt) {
        return Jwts.parser()
                .setSigningKey(signKey) // 指定签名密钥
                .parseClaimsJws(jwt) // 解析令牌
                .getBody();
    }
}