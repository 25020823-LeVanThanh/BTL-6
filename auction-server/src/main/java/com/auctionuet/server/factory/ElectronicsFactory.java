package com.auctionuet.server.factory;

import com.auctionuet.server.model.Item;
import com.auctionuet.server.model.Electronics;

public class ElectronicsFactory extends ItemFactory {
    private final String name;
    private final String description;
    private final String brand;
    private final int warrantyMonths;

    // Constructor
    public ElectronicsFactory(String name, String description, String brand, int warrantyMonths) {
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public Item createItem() {
        return new Electronics(name, description, brand, warrantyMonths);
    }
}
