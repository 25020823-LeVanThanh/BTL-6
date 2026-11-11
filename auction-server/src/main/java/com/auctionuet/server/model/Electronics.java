package com.auctionuet.server.model;

public class Electronics extends Product {
    private String brand;
    private int warrantyMonths;

    public Electronics() {
        this.category = Category.ELECTRONICS;
    }

    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public int getWarrantyMonths() {
        return warrantyMonths;
    }
    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }
}
