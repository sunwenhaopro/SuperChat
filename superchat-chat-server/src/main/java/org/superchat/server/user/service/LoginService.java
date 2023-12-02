package org.superchat.server.user.service;

public interface LoginService {
    void  refreshTokenExpire(String token);
    Long  getUidIfPresent(String token);
    String login(Long uid);
}
