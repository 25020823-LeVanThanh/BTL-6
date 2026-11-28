package com.auctionuet.server.repository.impl;
import com.auctionuet.server.model.Category; // Import model Category
import com.auctionuet.server.repository.CategoryRepository; // Import interface CategoryRepository
import com.fasterxml.jackson.core.type.TypeReference; // Giúp Jackson nhận diện kiểu List<Category>
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

// TODO(Người 1): implement đọc/ghi categories.json
public class CategoryRepositoryImpl implements CategoryRepository {
	// Đường dẫn tới file lưu trữ danh mục sản phẩm
	private static final String FILE_PATH = "auction-server/data/categories.json";

	// Khởi tạo bộ chuyển đổi JSON dùng riêng cho Repository này
	private final ObjectMapper mapper = new ObjectMapper()
			.registerModule(new JavaTimeModule()) // Hỗ trợ đọc/ghi kiểu thời gian LocalDateTime
			.enable(SerializationFeature.INDENT_OUTPUT); // Format file JSON thụt lề, xuống dòng dễ nhìn

	@Override
	public Category save(Category category) {
		List<Category> categories = findAll(); // Đọc toàn bộ danh mục hiện có từ file lên
		if (category.getId() == null || category.getId().isEmpty()) { // Nếu danh mục chưa có ID
			category.setId(UUID.randomUUID().toString()); // Tự động sinh mã ID ngẫu nhiên
			categories.add(category); // Thêm danh mục mới vào danh sách
		} else { // Nếu đã có ID (trường hợp cập nhật)
			categories.removeIf(c -> c.getId() != null && c.getId().equals(category.getId())); // Xóa bản ghi cũ trùng ID
			categories.add(category); // Thêm bản ghi mới cập nhật vào danh sách
		}
		writeToFile(categories); // Ghi toàn bộ danh sách xuống file categories.json
		return category; // Trả về đối tượng Category đã lưu
	}

	@Override
	public Optional<Category> findById(String id) {
		return findAll().stream() // Đọc danh sách và mở luồng duyệt từng phần tử
				.filter(c -> c.getId() != null && c.getId().equals(id)) // Lọc danh mục có ID trùng khớp
				.findFirst(); // Trả về phần tử đầu tiên tìm thấy
	}

	@Override
	public Optional<Category> findByName(String name) {
		return findAll().stream() // Đọc danh sách và mở luồng duyệt từng phần tử
				.filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(name)) // So sánh tên không phân biệt hoa thường
				.findFirst(); // Trả về danh mục đầu tiên khớp tên
	}

	@Override
	public List<Category> findAll() {
		File file = new File(FILE_PATH); // Trỏ tới đường dẫn file categories.json
		if (!file.exists() || file.length() == 0) { // Nếu file chưa tồn tại hoặc rỗng
			return new ArrayList<>(); // Trả về danh sách rỗng
		}
		try {
			return mapper.readValue(file, new TypeReference<List<Category>>() {}); // Đọc và ép kiểu về List<Category>
		} catch (IOException e) {
			throw new RuntimeException("Lỗi khi đọc file " + FILE_PATH, e); // Ném lỗi nếu đọc file thất bại
		}
	}

	@Override
	public void deleteById(String id) {
		List<Category> categories = findAll(); // Đọc danh sách hiện tại từ file
		boolean removed = categories.removeIf(c -> c.getId() != null && c.getId().equals(id)); // Xóa phần tử có ID trùng khớp
		if (removed) { // Chỉ ghi lại file nếu thực sự có phần tử bị xóa
			writeToFile(categories); // Ghi danh sách mới xuống file
		}
	}
	private void writeToFile(List<Category> categories) {
		File file = new File(FILE_PATH); // Trỏ tới file categories.json
		if (file.getParentFile() != null && !file.getParentFile().exists()) { // Kiểm tra thư mục cha đã tồn tại chưa
			file.getParentFile().mkdirs(); // Tự động tạo thư mục auction-server/data nếu chưa có
		}
		try {
			mapper.writeValue(file, categories); // Ghi đè danh sách xuống file
		} catch (IOException e) {
			throw new RuntimeException("Lỗi khi ghi file " + FILE_PATH, e); // Ném lỗi nếu ghi file thất bại
		}
	}
}
