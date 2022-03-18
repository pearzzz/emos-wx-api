package com.example.emos.wx.config.shiro;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Description
 * @Author pearz
 * @Email zhaihonghao317@163.com
 * @Date 10:33 2022/3/18
 */

@Component
@Slf4j
public class JwtUntil {
    @Value("${emos.jwt.secret}")
    private String secret;
    @Value("${emos.jwt.expire}")
    private int expire;

    public String createToken(int userId) {
        //过期时间（从今天起偏移5天）
        Date date = DateUtil.offset(new Date(), DateField.DAY_OF_YEAR, 5);
        //加密算法
        Algorithm algorithm = Algorithm.HMAC256(secret);
        //内部类
        JWTCreator.Builder builder = JWT.create();
        //绑定userID、过期时间、加密算法
        String token = builder.withClaim("userId", userId).withExpiresAt(date).sign(algorithm);
        return token;
    }

    public int getUserId(String token) {
        DecodedJWT decode = JWT.decode(token);
        int userId = decode.getClaim("userId").asInt();
        return userId;
    }

    public void verifierToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        verifier.verify(token);
    }
}
