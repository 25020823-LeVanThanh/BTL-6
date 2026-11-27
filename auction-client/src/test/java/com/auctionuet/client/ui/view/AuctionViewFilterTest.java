package com.auctionuet.client.ui.view;

import com.auctionuet.protocol.dto.response.auction.AuctionDTO;
import com.auctionuet.protocol.dto.response.item.ItemDTO;
import com.auctionuet.protocol.dto.response.user.UserDTO;
import com.auctionuet.protocol.enums.AuctionStatus;
import com.auctionuet.protocol.enums.ItemCondition;
import com.auctionuet.protocol.enums.ItemType;
import com.auctionuet.protocol.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionViewFilterTest {

    @Test
    void emptyInputYieldsNoRepresentative() {
        assertTrue(AuctionViewFilter.representativeAuctions(null).isEmpty());
        assertNull(AuctionViewFilter.representativeAuction(List.of()));
    }

    @Test
    void keepsOneAuctionPerItemInFirstSeenOrder() {
        AuctionDTO laptopRunning = auction("a-run", "item-laptop", AuctionStatus.RUNNING, hour(2));
        AuctionDTO laptopFinished = auction("a-old", "item-laptop", AuctionStatus.FINISHED, hour(5));
        AuctionDTO phone = auction("a-phone", "item-phone", AuctionStatus.OPEN, hour(3));

        List<AuctionDTO> selected = AuctionViewFilter.representativeAuctions(
                Arrays.asList(laptopFinished, phone, null, laptopRunning));

        assertEquals(List.of("a-run", "a-phone"), selected.stream().map(AuctionDTO::getId).toList());
    }

    @Test
    void prefersWaitingPaymentOverRunningThenNewerTime() {
        AuctionDTO running = auction("running", "item-1", AuctionStatus.RUNNING, hour(4));
        AuctionDTO waiting = auction("waiting", "item-1", AuctionStatus.WAITING_PAYMENT, hour(1));
        AuctionDTO laterFinished = auction("later", "item-1", AuctionStatus.FINISHED, hour(6));
        AuctionDTO earlierFinished = auction("earlier", "item-1", AuctionStatus.CANCELED, hour(2));

        assertEquals("waiting", AuctionViewFilter.representativeAuction(List.of(running, waiting)).getId());
        assertEquals("later", AuctionViewFilter.representativeAuction(List.of(earlierFinished, laterFinished)).getId());
    }

    @Test
    void blockingStatusesAreOpenRunningAndWaitingPayment() {
        assertTrue(AuctionViewFilter.isBlockingStatus(AuctionStatus.OPEN));
        assertTrue(AuctionViewFilter.isBlockingStatus(AuctionStatus.RUNNING));
        assertTrue(AuctionViewFilter.isBlockingStatus(AuctionStatus.WAITING_PAYMENT));
        assertFalse(AuctionViewFilter.isBlockingStatus(AuctionStatus.PAID));
        assertFalse(AuctionViewFilter.isBlockingStatus(AuctionStatus.FINISHED));
    }

    private static AuctionDTO auction(String id, String itemId, AuctionStatus status, LocalDateTime end) {
        UserDTO seller = new UserDTO("seller-1", "seller", UserRole.SELLER);
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("brand", "Dell");
        extra.put("warrantyMonths", 12);
        ItemDTO item = new ItemDTO(
                itemId, "Laptop", "Mo ta", 1_000_000, ItemType.ELECTRONICS,
                seller, null, ItemCondition.GOOD, extra);
        return new AuctionDTO(
                id, item, seller, null, null, 1_000_000, "Phien " + id, "Mo ta phien",
                end.minusHours(2), end, status, 60, 30);
    }

    private static LocalDateTime hour(int hourOfDay) {
        return LocalDateTime.of(2026, 11, 27, hourOfDay, 0);
    }
}
