package org.superchat.server.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    public static final String UID = "uid";
    public static final String DATE = "date";
    @Value("${SuperChat.jwt.secret}")
    private String secretKey;

    public String creatToken(Long  uid)
    {
        return JWT.create().withClaim(UID,uid)
                .withClaim(DATE,new Date())
                .sign(Algorithm.HMAC256(secretKey));
    }
}
