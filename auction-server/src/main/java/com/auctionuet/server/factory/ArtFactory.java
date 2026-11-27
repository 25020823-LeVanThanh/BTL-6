package com.auctionuet.server.factory;

import com.auctionuet.server.model.Item;
import com.auctionuet.server.model.Art;

public class ArtFactory extends ItemFactory {
    private final String name;
    private final String description;
    private final String artist;
    private final int yearCreated;

    // Constructor
    public ArtFactory(String name, String description, String artist, int yearCreated) {
        this.name = name;
        this.description = description;
        this.artist = artist;
        this.yearCreated = yearCreated;
    }

    @Override
    public Item createItem() {
        return new Art(name, description, artist, yearCreated);
    }
}
