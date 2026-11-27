/* VAI TRÒ FILE:
Thay vì để Controller tự gọi các constructor của từng lớp,
chúng ta giao nhiệm vụ tạo đối tượng cho các Factory.
 */

package com.auctionuet.server.factory;

import com.auctionuet.server.model.Item;

public abstract class ItemFactory {
    public abstract Item createItem();

}
