/*  VAI TRÒ FILE:
- Chia các sản phẩm thành các danh mục (Category) khác nhau để dễ quản lý
- Giúp người dùng dễ dàng lọc ra danh mục sản phẩm mình cần (VD: Đồ điện tử, văn phòng phẩm, sách báo,...)
- LƯU Ý: File chỉ mô tả và lưu thông tin danh mục trong Java. File chưa tạo bảng MySQL, lưu dữ liệu vào database hay tìm kiếm sản phẩm
 */

package com.auctionnet.server.model;    // Package xác định lớp thuộc gói com.auctionnet.server.model

public class Category {
    // KHỞI TẠO THUỘC TÍNH
    // Các thuộc tính khai báo private để đảm bảo tính đóng gói và được truy cập qua getter, setter.
    private Long id;               // Mã danh mục (VD: 9238741)
    // LƯU Ý: long: kiểu dữ liệu nguyên thủy (primitive type), không nhận giá trị null
    //        Long: lớp bao (wrapper class) của long, có thể nhận giá trị null
    private String name;           // Tên danh mục (VD: "Electronics")
    private String description;    // Mô tả danh mục (VD: "Các thiết bị điện tử")

        /*  CONSTRUCTOR OVERLOADING: NẠP CHỒNG HÀM TẠO
        - Cùng 1 lớp có thể có nhiều constructor khác nhau miễn khác danh sách tham số.
        - Constructor không tham số hữu ích khi cần tạo đối tượng trước rồi gán dữ liệu sau.
        => Linh hoạt hơn khi tạo đối tượng
         */

    // CONSTRUCTOR 1: Không tham số - Cho phép tạo một danh mục chưa có thông tin.
    public Category() {}

    // CONSTRUCTOR 2: 3 tham số - Cho phép tạo một danh mục và cung cấp thông tin ngay lập tức.
    public Category(Long id, String name, String description) {
            /* SỬ DỤNG HÀM SETTER
            - Có thể thay thế bẳng this.id = id; nhưng lúc này constructor sẽ chỉ gán được giá trị, không kiểm tra dữ liệu
            - Ví dụ nếu không sử dụng hàm Setter, khi người dùng nhập:
                    id = -12
                    name = "" (rỗng)
                    description = "Thiết bị điện tử"
             => Constructor sẽ không thể phát hiện lỗi
             */
        setId(id);
        setName(name);
        setDescription(description);
    }

        /* PHƯƠNG THỨC GETTER:
        - Lấy ID hiện tại của danh mục
        - Do các thuộc tính đều được khai báo private nên không thể truy cập trực tiếp bằng category.id
        => Gọi category.getId();
         => Đảm bảo tính đóng gói
         */
    public Long getId() {
        return id;
    }

        /* PHƯƠNG THỨC SETTER:
        - Giúp người dùng nhập vào danh mục
        - Kiểm tra xem danh mục nhập vào có hợp lệ hay không
        */
    public void setId(Long id) {
        if(id != null && id <= 0) {     // Kiểm tra ID đã có giá trị nhưng không phải số dương
            throw new IllegalArgumentException("Invalid ID");
        }
        // Sau khi qua được kiểm tra ngoại lệ thì lấy giá trị ID được truyền vào và lưu nó vào thuộc tính ID của đối tượng Category hiện tại
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(name == null || name.isBlank()) {    // Nếu tên danh mục bằng null hoặc tên chỉ chứa khoảng trắng/rỗng, thì tên không hợp lệ
            throw new IllegalArgumentException("Invalid category name");
        }
        this.name = name.trim();    // name.trim() cắt bỏ các khoảng trắng thừa ở đầu và cuối
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        // Nếu mô tả được truyền vào là null, thì lưu chuỗi rỗng "" vào thuộc tính của đối tượng
        if(description == null) { this.description = ""; }
        // Nếu description không phải null, chương trình lưu trực tiếp nội dung được truyền vào
        else { this.description = description; }
    }
}
