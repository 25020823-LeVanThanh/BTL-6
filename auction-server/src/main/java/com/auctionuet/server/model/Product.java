package com.auctionuet.server.model;
import java.math.BigDecimal;

public class Product {
    private Long id;
    private Item item;
    private Category category;
    private Seller seller;
    private ProductStatus status;
    private BigDecimal startingPrice;       // Sử dụng BigDecimal thay vì double giúp thực hiện các phép tính thập phân chính xác hơn
    /* LƯU Ý:
    - double được sử dụng để biểu diễn số thực với độ chính xác hữu hạn, phục vụ các phép tính cần tốc độ cao
    - Dù không biểu diễn chính xác mọi số thập phân, độ chính xác của double vẫn đủ cho nhiều ứng dụng.
    - Với tiền tệ, em ưu tiên BigDecimal để tránh sai số biểu diễn thập phân và kiểm soát quy tắc làm tròn.
     */

    // Constructor (tạm thời chưa đưa ID vào vì ID có thể được cấp khi bài đăng được lưu vào hệ thống)
    public Product(Item item, Category category, Seller seller, ProductStatus status, BigDecimal startingPrice) {
        setItem(item);
        setCategory(category);
        setSeller(seller);
        setStatus(status);
        setStartingPrice(startingPrice);
    }

    // Viết các hàm setter
    public void setItem (Item item) {
        // Ta chỉ xét trường hợp item == null do chỉ kiểm tra null để đảm bảo có đối tượng món hàng được truyền vào.
        // Tên và mô tả món hàng được xử lý theo quy tắc của lớp Item.
        if (item == null) {
            throw new IllegalArgumentException ("Invalid item");
        }
        else {
            this.item = item;
        }
    }
    public void setCategory (Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Invalid category");
        }
        else {
            this.category = category;
        }
    }
    public void setSeller (Seller seller) {
        if (seller == null) {
            throw new IllegalArgumentException("Invalid seller");
        }
        else {
            this.seller = seller;
        }
    }
    /* Sử dụng kiểu ProductStatus thay vì String vì:
    - Đây là kiểu liệt kê (enum) đã được định nghĩa trước với bốn trạng thái hợp lệ
    - Tránh trường hợp người dùng có thể gán status với 1 chuỗi khác (VD: status = "abcde")
    - Ta có thể viết: status = ProductStatus.PENDING;
     */
    public void setStatus (ProductStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Invalid status");
        }
        else {
            this.status = status;
        }
    }
    public void setStartingPrice (BigDecimal startingPrice) {
        if(startingPrice == null) {
            throw new IllegalArgumentException("Invalid starting price");
        } else if (startingPrice.compareTo(BigDecimal.ZERO) <= 0) {      // Biểu thức trả về true nếu startingPrice nhỏ hơn bằng 0
            throw new IllegalArgumentException("Invalid starting price");
        }
        else {
            this.startingPrice = startingPrice;
        }
    }

    // Viết các hàm Getter
    public Item getItem() {
        return item;
    }
    public Long getId() {
        return id;
    }
    public Category getCategory() {
        return category;
    }
    public Seller getSeller() {
        return seller;
    }
    public ProductStatus getStatus() {
        return status;
    }
    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

}