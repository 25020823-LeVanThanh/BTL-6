/*  VAI TRÒ CỦA FILE:
- Dựa trên lớp cha Item, thiết kế lớp con Art với các đặc điểm của tác phẩm nghệ thuật
 */

package com.auctionuet.server.model;    // Xác định lớp Art thuộc package com.auctionuet.server.model
import java.time.Year;                  // Thư viện để xác định ngày giờ

public class Art extends Item {
    private String artist;
    private int yearCreated;
    public Art(String name, String description, String artist, int yearCreated){
        super(name, description);           // Gọi constructor của Item để khởi tạo name và description
        setArtist(artist);                  // Tên nghệ sĩ của tác phẩm
        setYearCreated(yearCreated);        // Năm tạo ra tác phẩm
    }

    // Thiết kế lại phương thức trừu tượng của lớp cha
    @Override
    public String getItemType() {
        return "Art";
    }

    // Viết lại hàm Getter và Setter cho artist
    public String getArtist() {
        return artist;
    }
    public void setArtist(String artist) {
        if(artist == null || artist.isBlank()) {
            this.artist = "Unknown artist";
        }
        else {
            this.artist = artist.trim();
        }
    }

    // Viết lại hàm Getter và Setter cho yearCreated
    public int getYearCreated() {
        return yearCreated;
    }
    public void setYearCreated(int yearCreated) {
        // Nếu như năm tạo ra tác phẩm nhỏ hơn 0 hoặc lớn hơn năm hiện tại => Throw new exception
        if(yearCreated <= 0 || yearCreated > Year.now().getValue()) {
            throw new IllegalArgumentException("Invalid year");
        }
        this.yearCreated = yearCreated;
    }
}