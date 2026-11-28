# Protocol chung & chia việc ngang nhau (BTL-6)

> Tài liệu gửi cả nhóm. Khóa **tên file / method / field** trước khi code.
> Tham chiếu: `ROLES.md` · `docs/Hướng dẫn BTL LTNC-2026.pdf` · client `auction-client/.../network/*`

---

## Bản đồ nhanh

| Người | Domain | Protocol | Việc domain còn thiếu (ưu tiên) |
|:-----:|--------|----------|----------------------------------|
| **1** | Auth / User / Wallet / JSON | DTO auth·user·wallet | 5 `*RepositoryImpl` + `@Service` |
| **2** | Item / Catalog / Duyệt SP | DTO item·approval | **`ItemFactory`** + `setId` / PENDING |
| **3** | Auction / Bid / Anti-sniping | DTO auction·bid·push | **Push `AUCTION_EXTENDED`** + Observer→socket |
| **4** | Envelope / Admin / Đóng gói | `Request`/`Response`/`ActionType` | Socket router + `README` + CI |

```text
┌─────────────┐     JSON socket :3307      ┌──────────────────────────┐
│ auction-    │  Request / Response / Push │ auction-server           │
│ client      │ ─────────────────────────► │ RequestRouter → handlers │
│ (*Client)   │ ◄───────────────────────── │ (P1/P2/P3) + data JSON   │
└─────────────┘     PushMessage            └──────────────────────────┘
         ▲                                            ▲
         └──────── auction-protocol (DTO chung) ──────┘
```

---

## 1. Envelope dùng chung — **Người 4 làm trước (Ngày 1)**

Ai khác **không** sửa các file này sau khi merge, trừ khi báo nhóm.

| File | Nội dung khóa |
|------|----------------|
| `auction-protocol/.../Request.java` | `action`, `data`, `token` · ctor · `fromDto(ActionType, Object, String)` |
| `auction-protocol/.../Response.java` | `status`, `message`, `data` · `getDataAs` · `getDataListAs` · `ok` / `error` |
| `auction-protocol/.../PushMessage.java` | `pushType`, `data` · `getDataAs` |
| `auction-protocol/.../ActionType.java` | Enum **đủ** list mục 2 — chỉ Người 4 commit |
| `auction-protocol/.../PushActionType.java` | Xem bảng push bên dưới |
| `auction-protocol/.../util/NetworkGson.java` | `create()` — `LocalDateTime` ISO-8601 |

### Hằng số wire

| Tên | Giá trị |
|-----|---------|
| `status` OK | `"OK"` |
| `status` lỗi | `"ERROR"` / `"UNAUTHORIZED"` |
| Host / Port | `localhost` / **`3307`** |

### `PushActionType`

| Enum | Ai phát (server) | Ai lắng nghe (client) |
|------|------------------|------------------------|
| `BID_UPDATE` | Người 3 | Bidding / AuctionDetail |
| `AUCTION_EXTENDED` | Người 3 (**anti-sniping**) | BiddingController |
| `AUCTION_STARTED` | Người 3 | AuctionDetail |
| `AUCTION_ENDED` | Người 3 | Bidding / Detail |
| `AUCTION_CANCELED` | Người 3 | Bidding / Detail |
| `SYSTEM_MONITOR_UPDATED` | Người 4 | AdminSystemMonitor |

---

## 2. `ActionType` — phân theo người (Người 4 khai báo đủ)

### Người 1 — Auth / User / Wallet

`LOGIN` · `REGISTER` · `LOGOUT` · `GET_PROFILE` · `UPDATE_PROFILE` · `GET_WALLET` · `DEPOSIT` · `WITHDRAW` · `GET_MY_TRANSACTIONS` · `PING`

### Người 2 — Item / Duyệt

`CREATE_ITEM` · `GET_MY_ITEMS` · `DELETE_ITEM` · `GET_ITEM_AUCTION_HISTORY` · `GET_ITEMS_FOR_APPROVAL` · `UPDATE_ITEM_APPROVAL_STATUS` · `GET_ITEM_APPROVAL_SETTINGS` · `UPDATE_ITEM_APPROVAL_SETTINGS`

### Người 3 — Auction / Bid

`CREATE_AUCTION` · `START_AUCTION` · `GET_AUCTIONS` · `GET_AUCTION_DETAIL` · `PAY_AUCTION` · `GET_MY_PENDING_PAYMENTS` · `SUBSCRIBE` · `UNSUBSCRIBE` · `PLACE_BID` · `GET_BID_HISTORY` · `SET_AUTO_BID` · `CANCEL_AUTO_BID` · `CHECK_AUTO_BID`

### Người 4 — Admin hệ thống

`GET_ALL_USERS` · `UPDATE_USER_STATUS` · `ADMIN_CANCEL_AUCTION` · `GET_FINANCIAL_SUMMARY` · `GET_GLOBAL_TRANSACTIONS` · `SUBSCRIBE_SYSTEM_MONITOR` · `UNSUBSCRIBE_SYSTEM_MONITOR`

---

## 3. Cây file protocol (ai làm file nào)

```text
auction-protocol/src/main/java/com/auctionuet/protocol/
│
├── Request.java / Response.java / PushMessage.java     ──► Người 4
├── ActionType.java / PushActionType.java               ──► Người 4
├── util/NetworkGson.java                               ──► Người 4
│
├── enums/
│   ├── UserRole.java · AccountStatus.java              ──► Người 1
│   ├── TransactionType.java                            ──► Người 1
│   ├── ItemType.java · ItemCondition.java              ──► Người 2
│   ├── ItemApprovalStatus.java · ProductStatus.java    ──► Người 2
│   ├── AuctionStatus.java (+ WAITING_PAYMENT, PAID)    ──► Người 3
│   └── BidType.java · AutoBidStatus.java               ──► Người 3
│
├── dto/request/
│   ├── auth/   LoginRequestDTO · RegisterRequestDTO    ──► Người 1
│   ├── user/   UpdateProfileRequestDTO                 ──► Người 1
│   ├── wallet/ AmountRequestDTO                        ──► Người 1
│   ├── item/   CreateItemRequestDTO · ItemIdRequestDTO ──► Người 2
│   ├── admin/  UpdateItemApproval* · Get* (item)       ──► Người 2
│   ├── auction/ CreateAuctionRequestDTO · AuctionId*   ──► Người 3
│   ├── bid/    PlaceBid* · SetAutoBid* · AuctionId*    ──► Người 3
│   └── admin/  GetAllUsers* · UpdateUserStatus* · …    ──► Người 4
│
├── dto/response/
│   ├── auth/LoginResponseDTO · user/UserDTO            ──► Người 1
│   ├── wallet/WalletResponseDTO · transaction/*        ──► Người 1
│   ├── item/ItemDTO · admin/ItemApprovalSettingsDTO    ──► Người 2
│   ├── auction/AuctionDTO · bid/BidDTO · AutoBid*      ──► Người 3
│   └── admin/AdminUserDTO · Financial* · SystemMonitor*──► Người 4
│
└── dto/push/PushEvents.java
        BidUpdate · AuctionExtended · Started · Ended · Canceled ──► Người 3
        (payload SystemMonitor nếu tách)                         ──► Người 4
```

---

## 4. Field DTO bắt buộc (khớp client)

### Người 1

| Class | Field |
|-------|--------|
| `LoginRequestDTO` | `username`, `password` |
| `RegisterRequestDTO` | `username`, `password`, `fullName`, `email`, `role` |
| `LoginResponseDTO` | `token`, `user` |
| `UserDTO` | `id`, `username`, `fullName`, `email`, `role` |
| `AmountRequestDTO` | `amount` |
| `WalletResponseDTO` | `balance`, `frozenBalance`, `totalBalance` |
| `TransactionDTO` | `id`, `userId`, `auctionId`, `amount`, `type`, `timestamp`, `usernameSnapshot`, … |
| `TransactionType` | `DEPOSIT`, `WITHDRAW`, `HOLD`, `REFUND`, `PAYMENT` |

### Người 2

| Class | Field |
|-------|--------|
| `CreateItemRequestDTO` | `type`, `name`, `description`, `startingPrice`, `condition`, `imageUrl`, `extraFields` |
| `ItemIdRequestDTO` | `itemId` |
| `ItemDTO` | `id`, `type`, `name`, `description`, `startingPrice`, `condition`, `imageUrl`, `extraFields`, `approvalStatus`, … |
| `ItemType` | `ELECTRONICS`, `ART`, `VEHICLE` |

**`extraFields` keys khóa cứng** (đồng bộ model server Người 2 + `medium` cho ART)

| Type | Keys | Ghi chú |
|------|------|---------|
| `ELECTRONICS` | `brand`, `warrantyMonths` | Giữ nguyên — khớp `Electronics.java` |
| `ART` | `artist`, `yearCreated`, **`medium`** | `yearCreated` = field model; **Người 2 thêm** `medium` vào `Art.java` |
| `VEHICLE` | `brand`, `manufactureYear`, `odo` | Theo đúng `Vehicle.java` Người 2 (bỏ `make`/`model`/`mileage`/`vehicleYear`) |

**Người 4 (UI):** `CreateItemController` / FXML gửi đúng key bảng trên (Vehicle: `brand`+`manufactureYear`+`odo`; Art: `yearCreated` thay `year`).

### Người 3

| Class | Field |
|-------|--------|
| `CreateAuctionRequestDTO` | `itemId`, `startTime`, `endTime`, `title`, `description`, **`antiSnipingWindowSeconds`**, **`antiSnipingExtensionSeconds`** |
| `PlaceBidRequestDTO` | `auctionId`, `amount` |
| `SetAutoBidRequestDTO` | `auctionId`, `maxBid`, `increment` |
| `AuctionIdRequestDTO` | `auctionId` |
| `AuctionDTO` | `id`, `title`, `description`, `status`, `startTime`, `endTime`, `currentPrice`, `depositAmount`, `paymentDeadlineAt`, `canceledReason`, `seller`, `winner`, `item`, `currentHighestBid` |
| `BidDTO` | `id`, `auctionId`, `amount`, `timestamp`, `bidder`, `bidType` |
| `AuctionStatus` | `OPEN`, `RUNNING`, `FINISHED`, `WAITING_PAYMENT`, `PAID`, `CANCELED` |

**PushEvents (anti-sniping quan trọng)**

| Nested class | Field |
|--------------|--------|
| `BidUpdatePush` | `auctionId`, `bid` |
| **`AuctionExtendedPush`** | **`auctionId`**, **`newEndTime`** |
| `AuctionStartedPush` | `auctionId`, … |
| `AuctionEndedPush` | `auctionId`, `winner`, `finalPrice` |
| `AuctionCanceledPush` | `auctionId`, `reason` |

### Người 4

Admin: `AdminUserDTO`, `FinancialSummaryDTO`, `SystemMonitorDTO` + request filter/update tương ứng `AdminClient`.

---

## 5. Việc từng người (protocol + domain) — workload ngang nhau

### Người 1 — Auth / lưu dữ liệu

**Protocol**
- [ ] Enum + DTO mục Người 1 (§3–4)

**Domain (`ROLES.md`)**
- [ ] `UserRepositoryImpl` → `users.json`
- [ ] `ProductRepositoryImpl` → `products.json`
- [ ] `CategoryRepositoryImpl` → `categories.json`
- [ ] `AuctionRepositoryImpl` → `auctions.json`
- [ ] `BidRepositoryImpl` → `bids.json`
- [ ] Thư mục `auction-server/data/`
- [ ] `@Service` / `@Repository` cho service & repo
- [ ] `register` tạo `Bidder` / `Seller` / `Admin` theo role
- [ ] Handler socket: `LOGIN` … `GET_WALLET` …

**Done:** Đăng ký / đăng nhập / ví chạy qua socket; JSON ghi được đĩa.

---

### Người 2 — Catalog / Item

**Protocol**
- [ ] Enum + DTO mục Người 2 (§3–4)

**Domain (`ROLES.md` + đề)**
- [ ] **`ItemFactory`** — Factory Method tạo `Art` / `Electronics` / `Vehicle`
- [ ] **`Art.java`:** thêm field `medium` (+ getter/setter) cho khớp `extraFields`
- [ ] `Product.setId(...)`
- [ ] `ProductServiceImpl.create` ép `PENDING` + `@Service`
- [ ] `CategoryServiceImpl` + `@Service`
- [ ] `ProductSearchServiceImpl` dùng `ProductRepository` (không list inject)
- [ ] Handler socket: `CREATE_ITEM`, duyệt approval — map `extraFields` 1–1 theo bảng §4

**Done:** Tạo item theo type qua Factory; admin duyệt; search/repo thật; key extraFields khớp model.

---

### Người 3 — Engine + **Anti-sniping còn dở**

**Protocol**
- [ ] DTO auction / bid / `AuctionStatus` đủ / `PushEvents` (§3–4)

**Domain (file ROLES Người 3 — hoàn thiện)**

Đã có sẵn (giữ):
- `Auction.antiSnipingWindowSeconds` / `antiSnipingExtensionSeconds`
- `BidServiceImpl.applyAntiSniping(...)`
- `AuctionService.updateEndTime(...)`

Còn phải làm nốt:
- [ ] Khi extend `endTime` → `BidEventPublisher.publishAuctionExtended(auctionId, newEndTime)`
- [ ] Listener socket gửi `PushActionType.AUCTION_EXTENDED` + `AuctionExtendedPush`
- [ ] Observer: thay/bổ sung `ConsoleBidListener` → push `BID_UPDATE` / `AUCTION_ENDED` / `AUCTION_CANCELED` / `AUCTION_STARTED`
- [ ] Handler socket: `CREATE_AUCTION` (nhận 2 field anti-sniping), `PLACE_BID`, `SUBSCRIBE`, `GET_BID_HISTORY`, `PAY_AUCTION`, …
- [ ] (Tuỳ chọn +điểm) Auto-bid: `SET_AUTO_BID` / `CHECK_AUTO_BID`

**Done:** Bid trong cửa sổ cuối → giờ kết thúc tăng **và** mọi client subscribed nhận `AUCTION_EXTENDED` (đồng hồ client chạy lại). Realtime bid không chỉ in console.

---

### Người 4 — Envelope / Admin / đóng gói

**Protocol**
- [ ] `Request` / `Response` / `PushMessage` / `ActionType` / `PushActionType` / `NetworkGson`
- [ ] Admin DTO (§3–4)

**Domain**
- [ ] `README.md` — cài đặt & chạy server + client
- [ ] Khung socket: `AuctionServer` · `ClientHandler` · `RequestRouter` (switch `ActionType` → handler P1/P2/P3)
- [ ] CI `.github/workflows` compile/test xanh
- [ ] JUnit smoke cho envelope (`Request`/`Response`/`NetworkGson`)

**Done:** Client connect `:3307`, `PING`/`LOGIN` nhận đúng envelope; README đủ demo.

---

## 6. Lịch làm việc chung

| Mốc | Việc | Ai |
|-----|------|-----|
| **Ngày 1** | Merge envelope + `ActionType` + `NetworkGson` | Người 4 |
| **Ngày 1–2** | Viết DTO đúng package/field | Cả 4 (song song) |
| **Ngày 2–3** | Repo JSON · ItemFactory · Anti-sniping push · Socket router | P1 · P2 · P3 · P4 |
| **Ngày 3–4** | Nối handler + test 2 client (bid + extend) | Cả nhóm |
| **Ngày 4** | README + CI + checklist demo đề | Người 4 lead |

---

## 7. Quy tắc tránh đụng độ

1. Chỉ sửa file trong “ô” của mình (§3 + §5).
2. `ActionType` + envelope = **chỉ Người 4**.
3. Model server (`Auction`, `Bid`, …) vẫn theo `ROLES.md` — protocol chỉ là DTO wire.
4. Map model → DTO viết cạnh handler của từng người.
5. Branch gợi ý: `p1/protocol-auth` · `p2/item-factory` · `p3/anti-sniping-push` · `p4/envelope-socket`.
6. Đổi tên field sau khi merge → phải báo nhóm + PR riêng.

---

## 8. Checklist gửi nhanh (copy chat nhóm)

```text
Người 1: DTO auth/user/wallet + 5 RepositoryImpl JSON + @Service + register subclass
Người 2: DTO item/duyệt + ItemFactory + Art.medium + setId/PENDING/search-repo
Người 3: DTO auction/bid/push + nốt anti-sniping AUCTION_EXTENDED + Observer→socket
Người 4: Envelope + ActionType + socket router + README + CI; UI extraFields khớp §4 (P2)
```

---

## 9. Liên kết nội bộ

| File | Vai trò |
|------|---------|
| [`ROLES.md`](../ROLES.md) | Phân công file domain gốc |
| `docs/Hướng dẫn BTL LTNC-2026.pdf` | Rubric / timeline |
| `docs/2026-BTL.pdf` | Đề chức năng + pattern |
| Client `*Client.java` | Nguồn sự thật cho tên `ActionType` / DTO đang dùng |
