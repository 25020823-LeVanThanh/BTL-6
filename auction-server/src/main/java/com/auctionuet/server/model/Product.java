package com.auctionuet.server.model;
import java.io.Serializable;
public class Product implements Serializable {
    protected String id;
    protected String name;
    protected String description;
    protected double startingPrice;
    protected double currentPrice;
    protected Category category;
    protected ProductStatus status;
    protected String sellerId;

    public Product() {
        this.status = ProductStatus.PENDING; // Mặc định sản phẩm mới tạo ở trạng thái chờ duyệt
    }
    public Product(String id, String name, String description, double startingPrice, Category category, String sellerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.category = category;
        this.sellerId = sellerId;
        this.status = ProductStatus.PENDING;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public double getStartingPrice() {
        return startingPrice;
    }
    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }
    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }

    public ProductStatus getStatus() {
        return status;
    }
    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public String getSellerId() {
        return sellerId;
    }
    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }
}
