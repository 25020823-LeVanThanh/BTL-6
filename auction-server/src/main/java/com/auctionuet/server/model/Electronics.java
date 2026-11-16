/*  VAI TRÒ CỦA FILE:
- Dựa trên lớp cha Item, thiết kế lớp con Electronics với các đặc điểm của đồ điện tử
 */

package com.auctionuet.server.model;            // Xác định lớp Electronics thuộc package com.auctionuet.server.model

public class Electronics extends Item {
    private String brand;
    private int warrantyMonths;
    public Electronics (String name, String description, String brand, int warrantyMonths) {
        super(name, description);               // Gọi constructor của lớp cha Item để lớp con thừa kế
        setBrand(brand);                        // Kiểm tra và gán thương hiệu cho thiết bị
        setWarrantyMonths(warrantyMonths);      // Thời gian bảo hành
    }

    // Thiết kế lại phương thức trừu tượng của lớp cha
    @Override
    public String getItemType() {
        return "Electronics";
    }

    // Viết lại hàm Getter và Setter cho brand
    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) {
        if(brand == null || brand.isBlank()) {
            throw new IllegalArgumentException("Invalid brand name");
        }
        this.brand = brand.trim();
    }

    // Viết lại hàm Getter và Setter cho warrantyMonths
    public int getWarrantyMonths() {
        return warrantyMonths;
    }
    public void setWarrantyMonths(int warrantyMonths) {
        if(warrantyMonths < 0) {
            throw new IllegalArgumentException("Invalid warranty month");
        }
        this.warrantyMonths = warrantyMonths;
    }
}
