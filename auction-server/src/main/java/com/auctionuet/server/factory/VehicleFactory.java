package com.auctionuet.server.factory;

import com.auctionuet.server.model.Item;
import com.auctionuet.server.model.Vehicle;

public class VehicleFactory extends ItemFactory {
    private final String name;
    private final String description;
    private final String brand;
    private final int manufactureYear;
    private final double odo;

    // Constructor
    public VehicleFactory(String name, String description, String brand, int manufactureYear, double odo) {
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.manufactureYear = manufactureYear;
        this.odo = odo;
    }

    @Override
    public Item createItem() {
        return new Vehicle(name, description, brand, manufactureYear, odo);
    }
}
