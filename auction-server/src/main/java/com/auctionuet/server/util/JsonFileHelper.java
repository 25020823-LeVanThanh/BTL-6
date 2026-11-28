package com.auctionuet.server.util;
import com.fasterxml.jackson.core.type.TypeReference; // Hỗ trợ ép kiểu danh sách List<T> khi đọc JSON
import com.fasterxml.jackson.databind.ObjectMapper; // Thư viện Jackson dùng để chuyển đổi Object <-> JSON
import com.fasterxml.jackson.databind.SerializationFeature; // Cấu hình định dạng xuất file JSON
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Hỗ trợ đọc/ghi kiểu thời gian LocalDateTime

import java.io.File; // Thao tác với file và thư mục
import java.io.IOException; // Xử lý ngoại lệ đọc/ghi file
import java.util.ArrayList; // Trả về danh sách rỗng nếu file chưa có dữ liệu
import java.util.List; // Kiểu danh sách
public class JsonFileHelper {
    // Khởi tạo đối tượng mapper dùng chung toàn bộ chương trình
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Đăng ký module đọc/ghi ngày giờ chuẩn Java 8+
            .enable(SerializationFeature.INDENT_OUTPUT); // Xuất file JSON có thụt lề, xuống dòng dễ nhìn

    // Hàm đọc dữ liệu từ file JSON lên thành List<T>
    public static <T> List<T> readList(String filePath, TypeReference<List<T>> typeRef) {
        File file = new File(filePath); // Trỏ tới đường dẫn file trên ổ cứng
        if (!file.exists() || file.length() == 0) { // Nếu file chưa tồn tại hoặc dung lượng 0 byte (file trống)
            return new ArrayList<>(); // Trả về danh sách rỗng để tránh lỗi NullPointerException
        }
        try {
            return mapper.readValue(file, typeRef); // Đọc nội dung file JSON và chuyển thành List các Object
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file JSON: " + filePath, e); // Báo lỗi chi tiết nếu file bị hỏng định dạng
        }
    }

    // Hàm ghi toàn bộ List<T> xuống file JSON
    public static <T> void writeList(String filePath, List<T> data) {
        File file = new File(filePath); // Trỏ tới đường dẫn file cần ghi
        if (file.getParentFile() != null && !file.getParentFile().exists()) { // Kiểm tra thư mục cha (ví dụ: data/) đã có chưa
            file.getParentFile().mkdirs(); // Tự động tạo thư mục cha nếu chưa tồn tại (khắc phục lỗi thiếu thư mục data/)
        }
        try {
            mapper.writeValue(file, data); // Ghi đè toàn bộ danh sách mới xuống file JSON
        } catch (IOException e) {
            throw new RuntimeException("Lỗi ghi file JSON: " + filePath, e); // Báo lỗi nếu không thể ghi file
        }
    }
}
