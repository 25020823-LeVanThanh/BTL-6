package com.auctionuet.server.service.impl;
import com.auctionuet.server.model.Session;
import com.auctionuet.server.model.User;
import com.auctionuet.server.repository.UserRepository;
import com.auctionuet.server.service.SessionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionServiceImpl implements SessionService {
    // Lưu danh sách phiên đăng nhập trên RAM (Key: chuỗi token, Value: đối tượng Session)
    // Dùng ConcurrentHashMap để an toàn khi nhiều người dùng đăng nhập/đăng xuất cùng lúc
    private final Map<String, Session> activeSessions = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    public SessionServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Session createSession(User user) {
        String token = UUID.randomUUID().toString(); // Tạo mã token ngẫu nhiên duy nhất (36 ký tự)
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24); // Thiết lập thời gian hết hạn của phiên là sau 24 giờ
        Session session = new Session(token, user.getId(), expiresAt); // Khởi tạo đối tượng Session
        activeSessions.put(token, session); // Lưu phiên vào bộ nhớ RAM
        return session; // Trả về session (chứa token) cho client
    }

    @Override
    public Optional<Session> getSessionByToken(String token) {
        if (token == null || !activeSessions.containsKey(token)) { // Nếu token rỗng hoặc không có trong danh sách đang hoạt động
            return Optional.empty();
        }
        Session session = activeSessions.get(token);
        if (session.isExpired()) { // Nếu phiên đã quá 24 giờ (hết hạn)
            activeSessions.remove(token); // Xóa bỏ phiên hết hạn khỏi RAM cho nhẹ bộ nhớ
            return Optional.empty();
        }
        return Optional.of(session); // Trả về phiên hợp lệ
    }

    @Override
    public Optional<User> getUserByToken(String token) {
        return getSessionByToken(token) // Lấy session hợp lệ từ token
                .flatMap(session -> userRepository.findById(session.getUserId())); // Từ userId trong session, tìm ngược lại đối tượng User đầy đủ
    }

    @Override
    public boolean isValidSession(String token) {
        return getSessionByToken(token).isPresent(); // Trả về true nếu tìm thấy session còn hạn
    }

    @Override
    public void invalidateSession(String token) {
        if (token != null) {
            activeSessions.remove(token); // Xóa token khỏi Map -> người dùng chính thức đăng xuất
        }
    }
}
