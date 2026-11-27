package com.auctionuet.server.repository.impl;

import com.auctionuet.server.model.Product; // Import model Product
import com.auctionuet.server.model.ProductStatus; // Import enum trạng thái sản phẩm (nếu nằm ở package khác, hãy sửa lại đường dẫn import)
import com.auctionuet.server.repository.ProductRepository; // Import interface ProductRepository
import com.fasterxml.jackson.core.type.TypeReference; // Giúp Jackson nhận diện kiểu List<Product>
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
import java.util.stream.Collectors; // Gom kết quả lọc thành List

// TODO(Người 1): implement đọc/ghi products.json
public class ProductRepositoryImpl implements ProductRepository {
	// Đường dẫn tới file lưu trữ danh sách sản phẩm
	private static final String FILE_PATH = "auction-server/data/products.json";

	// Khởi tạo bộ chuyển đổi JSON dùng riêng cho Repository này
	private final ObjectMapper mapper = new ObjectMapper()
			.registerModule(new JavaTimeModule()) // Hỗ trợ đọc/ghi kiểu thời gian LocalDateTime
			.enable(SerializationFeature.INDENT_OUTPUT); // Format file JSON thụt lề, xuống dòng dễ nhìn

	@Override
	public Product save(Product product) {
		List<Product> products = findAll(); // Đọc toàn bộ sản phẩm hiện có từ file lên
		if (product.getId() == null || product.getId().isEmpty()) { // Nếu sản phẩm chưa có ID
			product.setId(UUID.randomUUID().toString()); // Sinh ID mới và gán cho sản phẩm
			products.add(product); // Thêm sản phẩm mới vào danh sách
		} else { // Nếu đã có ID (cập nhật)
			products.removeIf(p -> p.getId() != null && p.getId().equals(product.getId())); // Xóa bản ghi cũ trùng ID
			products.add(product); // Thêm bản ghi đã cập nhật vào danh sách
		}
		writeToFile(products); // Ghi toàn bộ danh sách xuống file products.json
		return product; // Trả về sản phẩm đã lưu
	}

	@Override
	public Optional<Product> findById(String id) {
		return findAll().stream() // Đọc danh sách và mở luồng duyệt
				.filter(p -> p.getId() != null && p.getId().equals(id)) // Lọc sản phẩm có ID trùng khớp
				.findFirst(); // Trả về phần tử đầu tiên tìm thấy
	}

	@Override
	public List<Product> findAll() {
		File file = new File(FILE_PATH); // Trỏ tới file products.json
		if (!file.exists() || file.length() == 0) { // Nếu file chưa tồn tại hoặc rỗng
			return new ArrayList<>(); // Trả về danh sách rỗng
		}
		try {
			return mapper.readValue(file, new TypeReference<List<Product>>() {}); // Đọc và ép kiểu về List<Product>
		} catch (IOException e) {
			throw new RuntimeException("Lỗi khi đọc file " + FILE_PATH, e); // Ném lỗi nếu đọc file thất bại
		}
	}

	@Override
	public List<Product> findBySellerId(String sellerId) {
		return findAll().stream() // Đọc danh sách và mở luồng duyệt
				.filter(p -> p.getSeller() != null && p.getSeller().getId() != null && p.getSeller().getId().equals(sellerId)) // Lấy object Seller rồi mới lấy ID để so sánh
				.collect(Collectors.toList()); // Gom các sản phẩm khớp thành List<Product>
	}

	@Override
	public List<Product> findByStatus(ProductStatus status) {
		return findAll().stream() // Đọc danh sách và mở luồng duyệt
				.filter(p -> p.getStatus() != null && p.getStatus() == status) // So sánh enum trạng thái
				.collect(Collectors.toList()); // Gom các sản phẩm cùng trạng thái thành List<Product>
	}

	@Override
	public void deleteById(String id) {
		List<Product> products = findAll(); // Đọc danh sách hiện tại
		boolean removed = products.removeIf(p -> p.getId() != null && p.getId().equals(id)); // Xóa phần tử có ID trùng khớp
		if (removed) { // Chỉ ghi lại file nếu thực sự có phần tử bị xóa
			writeToFile(products); // Ghi danh sách mới xuống file
		}
	}
	private void writeToFile(List<Product> products) {
		File file = new File(FILE_PATH); // Trỏ tới file products.json
		if (file.getParentFile() != null && !file.getParentFile().exists()) { // Kiểm tra thư mục cha đã tồn tại chưa
			file.getParentFile().mkdirs(); // Tự động tạo thư mục auction-server/data nếu chưa có
		}
		try {
			mapper.writeValue(file, products); // Ghi đè danh sách xuống file
		} catch (IOException e) {
			throw new RuntimeException("Lỗi khi ghi file " + FILE_PATH, e); // Ném lỗi nếu ghi file thất bại
		}
	}
}
