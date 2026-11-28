package com.auctionuet.server.repository.impl;

import com.auctionuet.server.model.User; // Import model User
import com.auctionuet.server.repository.UserRepository; // Import interface UserRepository
import com.fasterxml.jackson.core.type.TypeReference; // Giúp Jackson nhận diện kiểu List<User>
import com.fasterxml.jackson.databind.ObjectMapper; // Bộ chuyển đổi Object <-> JSON
import com.fasterxml.jackson.databind.SerializationFeature; // Cấu hình format file JSON
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Hỗ trợ đọc/ghi kiểu thời gian Java 8+
import org.springframework.stereotype.Repository; // Đánh dấu Spring Bean tầng Repository

import java.io.File; // Thao tác với file và thư mục
import java.io.IOException; // Xử lý ngoại lệ đọc/ghi file
import java.util.ArrayList; // Trả về danh sách rỗng nếu file chưa có dữ liệu
import java.util.List; // Kiểu danh sách
import java.util.Optional; // Bọc kết quả tìm kiếm tránh null
import java.util.UUID; // Sinh mã ID ngẫu nhiên

// TODO(Người 1): implement đọc/ghi users.json
public class UserRepositoryImpl implements UserRepository {
	// Đường dẫn tới file lưu trữ danh sách người dùng
	private static final String FILE_PATH = "auction-server/data/users.json";

	// Khởi tạo bộ chuyển đổi JSON dùng riêng cho Repository này
	private final ObjectMapper mapper = new ObjectMapper()
			.registerModule(new JavaTimeModule()) // Hỗ trợ đọc/ghi kiểu thời gian LocalDateTime
			.enable(SerializationFeature.INDENT_OUTPUT); // Format file JSON thụt lề, xuống dòng dễ nhìn

	@Override
	public User save(User user) {
		List<User> users = findAll(); // Đọc toàn bộ người dùng hiện có từ file lên
		if (user.getId() == null || user.getId().isEmpty()) { // Nếu người dùng chưa có ID
			user.setId(UUID.randomUUID().toString()); // Tự động sinh mã ID ngẫu nhiên
			users.add(user); // Thêm người dùng mới vào danh sách
		} else { // Nếu đã có ID (trường hợp cập nhật thông tin user)
			users.removeIf(u -> u.getId() != null && u.getId().equals(user.getId())); // Xóa bản ghi cũ trùng ID
			users.add(user); // Thêm bản ghi mới cập nhật vào danh sách
		}
		writeToFile(users); // Ghi toàn bộ danh sách xuống file users.json
		return user; // Trả về đối tượng User đã lưu
	}

	@Override
	public Optional<User> findById(String id) {
		return findAll().stream() // Đọc danh sách và mở luồng duyệt
				.filter(u -> u.getId() != null && u.getId().equals(id)) // Lọc user có ID trùng khớp
				.findFirst(); // Trả về phần tử đầu tiên tìm thấy
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return findAll().stream() // Đọc danh sách và mở luồng duyệt
				.filter(u -> u.getAccount().getUsername() != null && u.getAccount().getUsername().equalsIgnoreCase(username)) // So sánh username không phân biệt hoa thường
				.findFirst(); // Trả về user đầu tiên khớp username
	}

	@Override
	public List<User> findAll() {
		File file = new File(FILE_PATH); // Trỏ tới file users.json
		if (!file.exists() || file.length() == 0) { // Nếu file chưa tồn tại hoặc rỗng
			return new ArrayList<>(); // Trả về danh sách rỗng
		}
		try {
			return mapper.readValue(file, new TypeReference<List<User>>() {}); // Đọc và ép kiểu về List<User>
		} catch (IOException e) {
			throw new RuntimeException("Lỗi khi đọc file " + FILE_PATH, e); // Ném lỗi nếu đọc file thất bại
		}
	}

	@Override
	public void deleteById(String id) {
		List<User> users = findAll(); // Đọc danh sách hiện tại
		boolean removed = users.removeIf(u -> u.getId() != null && u.getId().equals(id)); // Xóa phần tử có ID trùng khớp
		if (removed) { // Chỉ ghi lại file nếu thực sự có phần tử bị xóa
			writeToFile(users); // Ghi danh sách mới xuống file
		}
	}
	private void writeToFile(List<User> users) {
		File file = new File(FILE_PATH); // Trỏ tới file users.json
		if (file.getParentFile() != null && !file.getParentFile().exists()) { // Kiểm tra thư mục cha đã tồn tại chưa
			file.getParentFile().mkdirs(); // Tự động tạo thư mục auction-server/data nếu chưa có
		}
		try {
			mapper.writeValue(file, users); // Ghi đè danh sách xuống file
		} catch (IOException e) {
			throw new RuntimeException("Lỗi khi ghi file " + FILE_PATH, e); // Ném lỗi nếu ghi file thất bại
		}
	}
}
