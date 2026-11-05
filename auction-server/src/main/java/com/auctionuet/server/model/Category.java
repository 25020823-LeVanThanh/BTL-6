package com.auctionnet.server.model;

public class Category {
    private Long id;
    private String name;
    private String description;

    // Constructor Overloading
    public Category() {}

    public Category(Long id, String name, String description) {
        setId(id);
        setName(name);
        setDescription(description);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        if(id != null && id <= 0) {
            throw new IllegalArgumentException("Invalid ID");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(name == null || name.isBlank()) {
            throw new IllegalArgumentException("Invalid product name");
        }
        this.name = name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if(description == null) { this.description = ""; }
        else { this.description = description; }
    }
}
