# Class Diagram — auction-server

Sơ đồ lớp của module `auction-server`, chia 4 phần: người dùng, sản phẩm, phiên đấu giá và giao dịch, tầng service/controller.

## 1. Người dùng & tài khoản

```mermaid
classDiagram
    class User {
        #String id
        #String fullName
        #String email
        #Role role
        #Account account
        #Wallet wallet
    }
    class Admin
    class Bidder {
        -String shippingAddress
    }
    class Seller {
        -String bankAccountNumber
    }
    class Account {
        -String username
        -String passwordHash
        -boolean active
    }
    class Wallet {
        -String walletId
        -double balance
    }
    class Session {
        -String token
        -String userId
        -LocalDateTime createdAt
        -LocalDateTime expiresAt
        +isExpired() boolean
    }
    class Role {
        <<enumeration>>
        ADMIN
        SELLER
        BIDDER
    }

    User <|-- Admin
    User <|-- Bidder
    User <|-- Seller
    User *-- Account
    User *-- Wallet
    User --> Role
    Session ..> User : userId
```

## 2. Sản phẩm & danh mục

```mermaid
classDiagram
    class Item {
        <<abstract>>
        -String name
        -String description
        +getItemType()* String
    }
    class Art {
        -String artist
        -int yearCreated
    }
    class Electronics {
        -String brand
        -int warrantyMonths
    }
    class Vehicle {
        -String brand
        -int manufactureYear
        -double odo
    }
    class Product {
        -Long id
        -Item item
        -Category category
        -Seller seller
        -ProductStatus status
        -BigDecimal startingPrice
    }
    class Category {
        -Long id
        -String name
        -String description
    }
    class ProductStatus {
        <<enumeration>>
        PENDING
        ACTIVE
        SOLD
        CANCELLED
    }

    Item <|-- Art
    Item <|-- Electronics
    Item <|-- Vehicle
    Product *-- Item
    Product --> Category
    Product --> Seller
    Product --> ProductStatus
```

## 3. Phiên đấu giá & giao dịch

```mermaid
classDiagram
    class Auction {
        -String id
        -String productId
        -String sellerId
        -double startingPrice
        -double currentPrice
        -double bidIncrement
        -String currentLeaderId
        -String winnerId
        -String status
        -LocalDateTime startTime
        -LocalDateTime endTime
    }
    class LiveAuction {
        -List~Bid~ bidHistory
        -int totalBids
        +addBid(Bid bid)
    }
    class Bid {
        -String id
        -String auctionId
        -String bidderId
        -double amount
        -LocalDateTime timestamp
    }
    class BidTransaction {
        -String id
        -String bidId
        -String userId
        -String auctionId
        -double amount
        -String type
        -LocalDateTime timestamp
    }

    Auction <|-- LiveAuction
    LiveAuction "1" *-- "*" Bid : bidHistory
    Auction ..> Product : productId
    Auction ..> Seller : sellerId
    Bid ..> Auction : auctionId
    Bid ..> Bidder : bidderId
    BidTransaction ..> Bid : bidId
    BidTransaction ..> User : userId
    BidTransaction ..> Auction : auctionId
```

- `status` của `Auction`: `OPEN → RUNNING → FINISHED → PAID`, hoặc `CANCELED`.
- `type` của `BidTransaction`: `DEPOSIT`, `WITHDRAW`, `HOLD`, `REFUND`, `PAYMENT`.
- Mũi tên nét đứt (`..>`) là liên kết qua id dạng `String`, không giữ object.

## 4. Service, controller & scheduler

```mermaid
classDiagram
    class AuctionService {
        <<interface>>
        +create(Auction) Auction
        +start(Auction)
        +finish(Auction)
        +cancel(Auction)
        +findById(String) Optional~Auction~
        +listAll() List~Auction~
        +getLive(String) Optional~LiveAuction~
    }
    class BidService {
        <<interface>>
        +placeBid(String auctionId, String bidderId, double amount) Bid
        +getBidHistory(String) List~Bid~
        +registerLive(LiveAuction)
        +unregisterLive(String)
    }
    class WalletService {
        <<interface>>
        +getBalance(String) double
        +deposit(String, double)
        +withdraw(String, double)
        +holdDeposit(String userId, String auctionId, double)
        +refundDeposit(String userId, String auctionId, double)
        +settlePayment(String winnerId, String sellerId, String auctionId, double finalPrice, double depositHeld)
    }
    class TransactionService {
        <<interface>>
        +record(String bidId, String userId, String auctionId, double, String type) BidTransaction
        +findByUser(String) List~BidTransaction~
        +findByAuctionId(String) List~BidTransaction~
        +listAll() List~BidTransaction~
    }

    class AuctionServiceImpl {
        -Map~String, Auction~ store
        -Map~String, LiveAuction~ liveMap
    }
    class BidServiceImpl {
        -Map~String, LiveAuction~ live
    }
    class WalletServiceImpl {
        -Map~String, Double~ balances
        -Map~String, Double~ holds
    }
    class TransactionServiceImpl {
        -List~BidTransaction~ store
    }

    class AuctionController {
        <<RestController>>
        /api/auctions
    }
    class BidController {
        <<RestController>>
        /api/auctions/auctionId/bids
    }
    class AuctionTimerTask {
        <<Component>>
        +tick()
    }

    AuctionService <|.. AuctionServiceImpl
    BidService <|.. BidServiceImpl
    WalletService <|.. WalletServiceImpl
    TransactionService <|.. TransactionServiceImpl

    AuctionServiceImpl --> BidService : register/unregister live
    AuctionServiceImpl --> WalletService : hoàn cọc, thanh toán
    BidServiceImpl --> WalletService : giữ cọc
    WalletServiceImpl --> TransactionService : ghi lịch sử

    AuctionController --> AuctionService
    BidController --> BidService
    AuctionTimerTask --> AuctionService : start / finish theo giờ
```

### Luồng chính
1. `AuctionController.create`: phiên mới ở trạng thái `OPEN`.
2. `AuctionTimerTask` đến `startTime` thì gọi `start`: phiên chuyển `RUNNING` và được đăng ký vào `BidService`.
3. `BidController.placeBid` → `BidService` kiểm tra, rồi `WalletService.holdDeposit` giữ cọc lần đầu.
4. `AuctionTimerTask` đến `endTime` thì gọi `finish`: hoàn cọc người thua, `settlePayment` cho người thắng, phiên chuyển `PAID`.

## Chưa có nội dung (interface / class rỗng)
- Service: `UserService`, `AuthService`, `SessionService`, `PermissionService`, `ProductService`, `CategoryService`, `ProductSearchService` và các `*Impl`.
- Repository: `UserRepository`, `ProductRepository`, `CategoryRepository`, `AuctionRepository`, `BidRepository`.
- Socket server: `AuctionServer`, `ClientHandler`, `RequestRouter`.
- Controller: `AuthController`, `ProductController`, `AdminProductController`.
