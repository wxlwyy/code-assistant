package com.wxl.agent.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.wxl.agent.model.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT 登录凭证工具类
 */
@Component
public class JwtUtils {

    // 密钥，用于 Token 加密签名
    @Value("${jwt.secret}")
    private String secret;  // 私有属性外部不能直接访问修改

    // Token 有效期：2 小时
    @Value("${jwt.expire-time}")
    private long expireTime;

    private JwtUtils() {}  // 外部不能new对象，只能使用spring容器创建的

    /**
     * 生成 Token
     */
    public String createToken(User user) {
        Date expireDate = new Date(System.currentTimeMillis() + expireTime);
        return JWT.create()
                // 把用户 ID 作为 Payload 存入 Token，后续解析Token时就能取出用户Id等信息，不用每次查数据库
                .withClaim("userId", user.getId())
                .withClaim("userName", user.getUserName())
                .withClaim("userRole", user.getUserRole())
                .withClaim("userAvatar", user.getUserAvatar())
                .withExpiresAt(expireDate)   // 设置过期时间
                .sign(Algorithm.HMAC256(secret)); // 使用 HMAC256 算法签名
    }

    // 解析 Token 同时，并在内存里还原出一个 User 实体，不需要查数据库
    public User getUserFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);

            User user = new User();
            user.setId(jwt.getClaim("userId").asLong());
            user.setUserName(jwt.getClaim("userName").asString());
            user.setUserRole(jwt.getClaim("userRole").asString());
            user.setUserAvatar(jwt.getClaim("userAvatar").asString());
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析并校验 Token，返回其中的 userId
     */
    public Long getUserId(String token) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
            return jwt.getClaim("userId").asLong();
        } catch (Exception e) {
            // Token 校验失败（过期、篡改等），返回 null
            return null;
        }
    }
}