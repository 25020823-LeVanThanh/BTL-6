package com.auctionuet.server.repository.impl;
import com.auctionuet.server.model.Auction; // Import model Auction (nếu model nằm ở package khác, hãy sửa lại đường dẫn import)
import com.auctionuet.server.repository.AuctionRepository; // Import interface AuctionRepository
import com.auctionuet.server.util.JsonFileHelper; // Import class tiện ích đọc/ghi JSON vừa tạo
import com.fasterxml.jackson.core.type.TypeReference; // Giúp Jackson nhận diện kiểu dữ liệu List<Auction>

import java.util.List; // Sử dụng List
import java.util.Optional; // Sử dụng Optional để bọc kết quả tìm kiếm
import java.util.UUID; // Dùng để tạo mã ID ngẫu nhiên duy nhất

// TODO(Người 1): implement đọc/ghi auctions.json
public class AuctionRepositoryImpl implements AuctionRepository {
	private static final String FILE_PATH = "auction-server/data/auctions.json";

	@Override
	public Auction save(Auction auction) {
		List<Auction> auctions = findAll(); // Đọc toàn bộ danh sách phiên đấu giá hiện có từ file lên
		if (auction.getId() == null || auction.getId().isEmpty()) { // Kiểm tra nếu đây là phiên đấu giá mới chưa có ID
			auction.setId(UUID.randomUUID().toString()); // Tự động sinh chuỗi ID ngẫu nhiên và gán cho auction
			auctions.add(auction); // Thêm phiên đấu giá mới vào danh sách
		} else { // Trường hợp cập nhật phiên đấu giá đã có sẵn ID
			auctions.removeIf(a -> a.getId() != null && a.getId().equals(auction.getId())); // Xóa bản ghi cũ có cùng ID
			auctions.add(auction); // Thêm bản ghi đã cập nhật vào danh sách
		}
		JsonFileHelper.writeList(FILE_PATH, auctions); // Ghi toàn bộ danh sách mới xuống file auctions.json
		return auction; // Trả về đối tượng Auction sau khi đã lưu thành công
	}

	@Override
	public Optional<Auction> findById(String id) {
		return findAll().stream() // Đọc danh sách và mở luồng duyệt từng phần tử
				.filter(a -> a.getId() != null && a.getId().equals(id)) // Giữ lại phiên đấu giá có ID khớp với tham số truyền vào
				.findFirst(); // Lấy phần tử đầu tiên tìm thấy (trả về Optional.empty() nếu không có)
	}
	@Override
	public Optional<Auction> findByProductId(String productId) {
		return findAll().stream() // Đọc danh sách và mở luồng duyệt từng phần tử
				.filter(a -> a.getProductId() != null && a.getProductId().equals(productId)) // Lọc ra phiên đấu giá của sản phẩm có mã tương ứng
				.findFirst(); // Trả về phiên đấu giá tìm thấy đầu tiên
	}

	@Override
	public List<Auction> findAll() {
		return JsonFileHelper.readList(FILE_PATH, new TypeReference<List<Auction>>() {}); // Đọc file auctions.json và ép kiểu về List<Auction>
	}

	@Override
	public void deleteById(String id) {
		List<Auction> auctions = findAll(); // Đọc danh sách hiện tại từ file
		boolean removed = auctions.removeIf(a -> a.getId() != null && a.getId().equals(id)); // Xóa phần tử có ID trùng khớp
		if (removed) { // Chỉ thực hiện ghi lại file nếu thực sự có phần tử bị xóa
			JsonFileHelper.writeList(FILE_PATH, auctions); // Lưu danh sách mới (sau khi đã xóa) xuống file auctions.json
		}
	}
}
