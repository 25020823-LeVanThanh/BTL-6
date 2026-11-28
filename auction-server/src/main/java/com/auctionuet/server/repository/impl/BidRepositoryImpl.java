package com.auctionuet.server.repository.impl;
import com.auctionuet.server.model.Bid; // Import model Bid
import com.auctionuet.server.repository.BidRepository; // Import interface BidRepository
import com.fasterxml.jackson.core.type.TypeReference; // Giúp Jackson nhận diện kiểu dữ liệu List<Bid>
import com.fasterxml.jackson.databind.ObjectMapper; // Bộ chuyển đổi giữa Object Java và chuỗi JSON
import com.fasterxml.jackson.databind.SerializationFeature; // Cấu hình format file JSON
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Hỗ trợ đọc/ghi kiểu thời gian LocalDateTime
import org.springframework.stereotype.Repository; // Đánh dấu Spring Bean tầng Repository

import java.io.File; // Làm việc với đường dẫn file và thư mục
import java.io.IOException; // Xử lý ngoại lệ đọc/ghi file
import java.util.ArrayList; // Trả về danh sách rỗng nếu file chưa có dữ liệu
import java.util.Comparator; // Dùng để so sánh tìm giá đặt cao nhất
import java.util.List; // Kiểu danh sách
import java.util.Optional; // Bọc kết quả tìm kiếm tránh lỗi null
import java.util.UUID; // Sinh mã ID ngẫu nhiên
import java.util.stream.Collectors; // Gom kết quả lọc thành List

// TODO(Người 1): implement đọc/ghi bids.json
public class BidRepositoryImpl implements BidRepository {
	private static final String FILE_PATH = "auction-server/data/bids.json";
	private final ObjectMapper mapper = new ObjectMapper()
			.registerModule(new JavaTimeModule()) // Hỗ trợ đọc/ghi kiểu thời gian LocalDateTime
			.enable(SerializationFeature.INDENT_OUTPUT); // Format file JSON thụt lề, xuống dòng dễ đọc

	@Override
	public Bid save(Bid bid) {
		List<Bid> bids = readFromFile(); // Đọc toàn bộ danh sách lượt đặt giá hiện có từ file lên
		if (bid.getId() == null || bid.getId().isEmpty()) { // Nếu lượt đặt giá chưa có ID
			bid.setId(UUID.randomUUID().toString()); // Tự động sinh mã ID ngẫu nhiên
			bids.add(bid); // Thêm lượt đặt giá mới vào danh sách
		} else { // Nếu đã có ID (cập nhật)
			bids.removeIf(b -> b.getId() != null && b.getId().equals(bid.getId())); // Xóa bản ghi cũ trùng ID
			bids.add(bid); // Thêm bản ghi mới vào danh sách
		}
		writeToFile(bids); // Ghi toàn bộ danh sách mới xuống file bids.json
		return bid; // Trả về đối tượng Bid đã lưu
	}

	@Override
	public Optional<Bid> findById(String id) {
		return readFromFile().stream() // Đọc file và mở luồng duyệt danh sách
				.filter(b -> b.getId() != null && b.getId().equals(id)) // Lọc lượt đặt giá có ID khớp tham số
				.findFirst(); // Trả về phần tử đầu tiên tìm thấy
	}

	@Override
	public List<Bid> findByAuctionId(String auctionId) {
		return readFromFile().stream() // Đọc file và mở luồng duyệt danh sách
				.filter(b -> b.getAuctionId() != null && b.getAuctionId().equals(auctionId)) // Lọc theo mã phiên đấu giá
				.collect(Collectors.toList()); // Gom tất cả kết quả khớp thành List<Bid>
	}


	@Override
	public List<Bid> findByBidderId(String bidderId) {
			return readFromFile().stream() // Đọc file và mở luồng duyệt danh sách
					.filter(b -> b.getBidderId() != null && b.getBidderId().equals(bidderId)) // Lọc theo mã người đặt giá
					.collect(Collectors.toList()); // Gom tất cả kết quả khớp thành List<Bid>
	}

	@Override
	public Optional<Bid> findHighestBidByAuctionId(String auctionId) {
		return findByAuctionId(auctionId).stream() // Lấy danh sách các lượt đặt giá thuộc phiên đấu giá này
				.max(Comparator.comparingDouble(Bid::getAmount)); // So sánh trường số tiền (amount) để tìm lượt cao nhất (nếu field trong Bid tên khác như getBidAmount, hãy sửa lại tên getter)
	}
	// Hàm nội bộ đọc danh sách từ file bids.json
	private List<Bid> readFromFile() {
		File file = new File(FILE_PATH); // Trỏ tới file bids.json
		if (!file.exists() || file.length() == 0) { // Nếu file chưa tồn tại hoặc rỗng
			return new ArrayList<>(); // Trả về danh sách rỗng
		}
		try {
			return mapper.readValue(file, new TypeReference<List<Bid>>() {}); // Đọc và ép kiểu về List<Bid>
		} catch (IOException e) {
			throw new RuntimeException("Lỗi khi đọc file " + FILE_PATH, e); // Ném lỗi nếu đọc thất bại
		}
	}
	// Hàm nội bộ ghi danh sách xuống file bids.json
	private void writeToFile(List<Bid> bids) {
		File file = new File(FILE_PATH); // Trỏ tới file bids.json
		if (file.getParentFile() != null && !file.getParentFile().exists()) { // Kiểm tra thư mục cha đã tồn tại chưa
			file.getParentFile().mkdirs(); // Tự động tạo thư mục auction-server/data nếu chưa có
		}
		try {
			mapper.writeValue(file, bids); // Ghi đè danh sách xuống file
		} catch (IOException e) {
			throw new RuntimeException("Lỗi khi ghi file " + FILE_PATH, e); // Ném lỗi nếu ghi thất bại
		}
	}
}
