/*  VAI TRÒ CỦA FILE:
- Item là lớp trừu tượng
- Chứa những thông tin chung của món hàng như tên và mô tả để lớp con kế thừa
 */

package com.auctionuet.server.model;    // Xác định lớp Item thuộc package com.auctionuet.server.model

public abstract class Item {    // Khai báo lớp trừu tượng Item
    private String name;
    private String description;

    public Item (String name, String description) {
        setName(name);
        setDescription(description);
    }

    // Phần này tương tự file Category.java, override các hàm Getter và Setter.
    public String getName() {
        return name;
    }
    public void setName(String name) {
        if(name == null || name.isBlank()) {
            throw new IllegalArgumentException("Invalid name");
        }
        this.name = name.trim();
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        if(description == null) {
            this.description = "";
        }
        else {
            this.description = description.trim();
        }
    }

    /* PHƯƠNG THỨC GETITEMTYPE():
    - Là phương thức mới được khai báo, chưa được triển khai (do đó không có thân hàm {})
    - Là phương thức được viết ra như 1 bản thiết kế để lớp con có thể triển khai riêng theo từng lớp.
    - Lớp con muốn triển khai nên sử dụng @Override để trình biên dịch giúp kiểm tra việc ghi đè phương thức.
     */
    public abstract String getItemType();
}
