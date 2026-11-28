package com.auctionuet.server.service;
import com.auctionuet.server.model.Session;
import com.auctionuet.server.model.User;
import java.util.Optional;
public interface SessionService {
    Session createSession(User user);
    Optional<Session> getSessionByToken(String token);
    Optional<User> getUserByToken(String token);
    boolean isValidSession(String token);
    void invalidateSession(String token);
}
