package com.auctionuet.server.model;

public class Vehicle extends Product {
    private String make;          // Hãng sản xuất (Honda, Toyota, BMW,...)
    private String model;         // Dòng xe (Audi,Mercedes,...)
    private Integer modelYear;    // Năm sản xuất (dùng Integer để xử lý số học và null an toàn)
    private Double mileage;       // Số km / dặm đã chạy
    private String licensePlate;  // Biển số xe hoặc mã định danh phương tiện

    public Vehicle() {
        super();
        this.category = Category.VEHICLE;
    }

    public Vehicle(String id, String name, String description, double startingPrice,
                   String sellerId, String make, String model, Integer modelYear,
                   Double mileage, String licensePlate) {
        super(id, name, description, startingPrice, Category.VEHICLE, sellerId);
        this.make = make;
        this.model = model;
        this.modelYear = modelYear;
        this.mileage = mileage;
        this.licensePlate = licensePlate;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getModelYear() {
        return modelYear;
    }

    public void setModelYear(Integer modelYear) {
        this.modelYear = modelYear;
    }

    public Double getMileage() {
        return mileage;
    }

    public void setMileage(Double mileage) {
        this.mileage = mileage;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
}
