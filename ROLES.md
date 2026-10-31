# Phân công theo file (nhìn tên file = biết chỗ làm)

Giữ kiến trúc **Client–Server + JavaFX** theo đề. Mỗi người chỉ sửa file đánh dấu của mình (trừ khi nhóm thống nhất).

## Người 1 — Tài khoản, quyền, lưu dữ liệu

| File | Việc làm |
|------|----------|
| `auction-server/.../model/User.java` | Thông tin user, role, số dư |
| `auction-server/.../model/Role.java` | ADMIN / SELLER / BIDDER |
| `auction-server/.../model/Account.java` | Tài khoản đăng nhập |
| `auction-server/.../model/Bidder.java` | Vai trò bidder |
| `auction-server/.../model/Seller.java` | Vai trò seller |
| `auction-server/.../model/Admin.java` | Vai trò admin |
| `auction-server/.../model/Session.java` | Phiên làm việc |
| `auction-server/.../model/Wallet.java` | Số dư (entity; logic trừ/hoàn → Người 3) |
| `auction-server/.../repository/*` | **Toàn bộ** đọc/ghi File-JSON/DB |
| `auction-server/.../service/UserService.java` + `impl/UserServiceImpl.java` | CRUD user |
| `auction-server/.../service/AuthService.java` + `impl/AuthServiceImpl.java` | Đăng ký / đăng nhập |
| `auction-server/.../service/PermissionService.java` + `impl/...` | Kiểm tra quyền |
| `auction-server/.../service/SessionService.java` + `impl/...` | Quản lý session |
| `auction-server/.../network/controller/AuthController.java` | Nhận request auth từ client |
| `auction-server/.../util/PasswordUtils.java` | Hash / kiểm tra mật khẩu |
| `auction-server/data/` | File dữ liệu JSON |

## Người 2 — Catalog sản phẩm & đăng bài

| File | Việc làm |
|------|----------|
| `auction-server/.../model/Product.java` | Sản phẩm / phiên đăng |
| `auction-server/.../model/Category.java` | Danh mục |
| `auction-server/.../model/ProductStatus.java` | PENDING / ACTIVE / SOLD / CANCELLED |
| `auction-server/.../model/Item.java` (+ Electronics/Art/Vehicle) | Cây kế thừa Item theo đề |
| `auction-server/.../service/ProductService.java` + `impl/...` | Thêm/sửa/xóa, giá sàn, bước giá, thời gian |
| `auction-server/.../service/CategoryService.java` + `impl/...` | Quản lý danh mục |
| `auction-server/.../service/ProductSearchService.java` + `impl/...` | Lọc / tìm kiếm |
| `auction-server/.../network/controller/ProductController.java` | API seller đăng bài |
| `auction-server/.../network/controller/AdminProductController.java` | Admin duyệt sản phẩm |
| `auction-server/.../exception/ProductNotFoundException.java` | Lỗi sản phẩm |

> Người 2 **gọi** `ProductRepository` / `CategoryRepository` do Người 1 viết — không tự đổi cách lưu file.

## Người 3 — Engine đấu giá & giao dịch

| File | Việc làm |
|------|----------|
| `auction-server/.../model/Bid.java` | Một lượt đặt giá |
| `auction-server/.../model/BidTransaction.java` | Giao dịch / lịch sử |
| `auction-server/.../model/Auction.java` | Phiên đấu giá |
| `auction-server/.../model/LiveAuction.java` | Phiên đang chạy |
| `auction-server/.../service/AuctionService.java` + `impl/...` | Luồng phiên đấu giá |
| `auction-server/.../service/BidService.java` + `impl/...` | Đặt giá, kiểm tra hợp lệ, concurrency |
| `auction-server/.../service/WalletService.java` + `impl/...` | Trừ cọc / hoàn tiền / thanh toán |
| `auction-server/.../service/TransactionService.java` + `impl/...` | Lịch sử giao dịch |
| `auction-server/.../scheduler/AuctionTimerTask.java` | Tự mở/đóng phiên, chọn người thắng |
| `auction-server/.../network/controller/BidController.java` | Request đặt giá |
| `auction-server/.../network/controller/AuctionController.java` | Request trạng thái phiên |
| `auction-server/.../exception/InvalidBidException.java` | Bid không hợp lệ |
| `auction-server/.../exception/AuctionClosedException.java` | Đấu khi phiên đóng |

> Người 3 **không** sửa logic duyệt sản phẩm (Người 2) hay cách lưu JSON (Người 1).

## Người 4 — UI/UX JavaFX, thông báo, đóng gói

| File | Việc làm |
|------|----------|
| `auction-client/.../ClientApp.java` / `Launcher.java` | Chạy app client |
| `auction-client/.../ui/MenuNavigator.java` | Điều hướng màn hình theo role |
| `auction-client/.../ui/view/AuthView.java` + `fxml/auth.fxml` | Đăng nhập / đăng ký |
| `auction-client/.../ui/view/AdminView.java` + `admin.fxml` | Duyệt sản phẩm |
| `auction-client/.../ui/view/SellerView.java` + `seller.fxml` | Đăng / quản lý bài |
| `auction-client/.../ui/view/AuctionRoomView.java` + `auction-room.fxml` | Phòng đấu realtime |
| `auction-client/.../ui/view/AuctionListView.java` + `auction-list.fxml` | Danh sách phiên |
| `auction-client/.../ui/view/ProductDetailView.java` + `product-detail.fxml` | Chi tiết sản phẩm |
| `auction-client/.../ui/util/NotificationHelper.java` | Thông báo bị outbid |
| `auction-client/.../ui/util/ConsolePrinter.java` | Format bảng / log UI (nếu cần) |
| `auction-client/.../network/*` | Kết nối socket phía client |
| `auction-client/.../util/MockDataUtil.java` | Fake data để test UI khi backend chưa xong |
| `README.md` | Hướng dẫn cài đặt / chạy |

## File dùng chung (thống nhất nhóm trước khi sửa)

| File | Ai |
|------|----|
| `auction-protocol/**` | Cả nhóm thống nhất message JSON/Socket |
| `auction-server/.../network/server/*` | Thường Người 3 hoặc 1 lead socket server |
| `pom.xml` (root + 3 module) | Cả nhóm / Người 4 hỗ trợ đóng gói |
| `.github/workflows/` | CI — Người 4 hoặc ai phụ trách test |
| `docs/` | Tài liệu đề (không sửa lung tung) |

## Quy tắc tránh đụng độ

1. **1 chức năng = 1 file** (interface + impl nếu là service).
2. Người 2–3 **chỉ gọi** repository của Người 1, không viết I/O riêng.
3. Người 4 **chỉ gọi** API qua `network/` + protocol — không đụng `repository/`.
4. Muốn đổi tên/thêm file: ghi vào PR / báo nhóm trước.
