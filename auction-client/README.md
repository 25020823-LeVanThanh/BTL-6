# AuctionUET Client

Ứng dụng JavaFX cho người đấu giá, người bán và quản trị. Client nói chuyện với server qua TCP, dùng JSON của module `auction-protocol`.

## Yêu cầu

- JDK 21
- Maven 3.9 trở lên
- Server AuctionUET đang lắng nghe `localhost:3307` (host và cổng mặc định trong `ServerConnection`)

Module này không build một mình. `pom.xml` kế thừa project cha và phụ thuộc `auction-protocol`, nên các lệnh dưới đây chạy từ thư mục gốc của repo.

## Cài dependency và biên dịch

```powershell
cd D:\BTLclone\BTL-6
mvn -pl auction-client -am clean install -DskipTests
```

`-am` build `auction-protocol` trước, rồi mới build client.

## Chạy giao diện

```powershell
mvn -pl auction-client -am javafx:run
```

Cửa sổ mở màn hình chính. Đăng nhập gửi `LOGIN` tới `localhost:3307`. Đăng ký, ví, phiên đấu giá và khu vực admin dùng cùng kết nối đó.

Gói chạy độc lập sau khi đã `package`:

```powershell
mvn -pl auction-client -am package -DskipTests
java -jar auction-client\target\auction-client-1.0-SNAPSHOT.jar
```

`Launcher` là main class của file JAR.

## Kiểm thử

```powershell
mvn -pl auction-client -am test
```

JUnit 5 nằm trong `auction-client/src/test/java`. Surefire của module này chạy các lớp `*Test`.

## Realtime

Các màn đặt giá, chi tiết phiên và giám sát hệ thống đăng ký `ServerConnection.PushListener`. Khi server gửi `PushMessage`, client cập nhật giá, gia hạn, kết thúc hoặc hủy phiên trên luồng JavaFX. Client không tự bịa sự kiện; không có push từ server thì các màn đó chỉ đổi sau lần tải lại.
