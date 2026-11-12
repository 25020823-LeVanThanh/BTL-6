package com.auctionuet.server.model;

public class Art extends Product{
    private String artist;
    private int creationYear;//năm ra đời
    private String medium; //chất liệu làm tranh
    public Art() {
        super();
        this.category = Category.ART;
    }
    public Art(String id, String name, String description, double startingPrice,
               String sellerId, String artist, Integer creationYear, String medium) {
        super(id, name, description, startingPrice, Category.ART, sellerId);
        this.artist = artist;
        this.creationYear = creationYear;
        this.medium = medium;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Integer getCreationYear() {
        return creationYear;
    }

    public void setCreationYear(Integer creationYear) {
        this.creationYear = creationYear;
    }

    public String getMedium() {
        return medium;
    }
    public void setMedium(String medium) {
        this.medium= medium;
    }
}
