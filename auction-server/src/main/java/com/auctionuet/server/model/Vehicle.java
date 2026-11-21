/*  VAI TRÒ CỦA FILE:
- Dựa trên lớp cha Item, thiết kế lớp con Vehicle
- Có các đặc điểm riêng của phương tiện giao thông.
 */

package com.auctionuet.server.model;
import java.time.Year;                              // Thư viện xác định ngày giờ

public class Vehicle extends Item {

    private String brand;                           // Tên hãng xe
    private int manufactureYear;                    // Năm sản xuất
    private double odo;                             // Số km đã đi

    // Constructor của lớp Vehicle
    public Vehicle(String name, String description, String brand, int manufactureYear, double odo) {
        super(name, description);
        setBrand(brand);
        setManufactureYear(manufactureYear);
        setOdo(odo);
    }

    // Ghi đè phương thức trừu tượng của lớp cha
    @Override
    public String getItemType() {
        return "Vehicle";
    }

    // Getter và Setter của brand
    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        // Nếu không nhập hãng xe thì dùng tên mặc định
        if (brand == null || brand.isBlank()) {
            this.brand = "Unknown brand";
        } else {
            this.brand = brand.trim();
        }
    }

    // Getter và Setter của manufactureYear
    public int getManufactureYear() {
        return manufactureYear;
    }

    public void setManufactureYear(int manufactureYear) {
        // Năm sản xuất phải dương và không vượt quá năm hiện tại
        if (manufactureYear <= 0 ||
                manufactureYear > Year.now().getValue()) {
            throw new IllegalArgumentException("Invalid year");
        }
        else {
            this.manufactureYear = manufactureYear;
        }
    }

    // Getter và Setter của odo
    public double getOdo() {
        return odo;
    }

    public void setOdo(double odo) {
        // Số km đã đi không được âm
        if (odo < 0) {
            throw new IllegalArgumentException("Invalid odo");
        }
        else {
            this.odo = odo;
        }
    }
}
