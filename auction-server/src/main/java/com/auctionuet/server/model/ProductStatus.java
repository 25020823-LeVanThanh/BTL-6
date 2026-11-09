/*  VAI TRÒ FILE:
- Định nghĩa các trạng thái hợp lệ của sản phẩm khi giao dịch
- Khi đăng 1 sản phẩm lên sẽ trải qua 3 trạng thái:
    + PENDING: Chờ admin duyệt qua sản phẩm
    + ACTIVE: Sản phẩm đã được duyệt, đang chờ bán
    + SOLD: Bán thành công
    + CANCELLED: Sản phẩm bị hủy

- LƯU Ý: Không phải sản phẩm nào cũng trải qua tất cả các trạng thái.
 */

package com.auctionet.server.model;

public enum ProductStatus {
    PENDING,
    ACTIVE,
    SOLD,
    CANCELLED
}