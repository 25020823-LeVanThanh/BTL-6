//đồng hồ hệ thống cho phiên đấu giá: tự mở/đóng theo thời gian, không cần người bấm tay.
/*
Mỗi X giây:
  OPEN + now >= startTime  → auctionService.start(a)
  RUNNING + now >= endTime → auctionService.finish(a)
*/

package com.auctionuet.server.scheduler;

import com.auctionuet.server.model.Auction;
import com.auctionuet.server.model.LiveAuction;
import com.auctionuet.server.service.AuctionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component//báo cho Spring: “hãy tự tạo một đối tượng của class này và quản lý nó”. Nhờ vậy Spring mới phát hiện và chạy các method có @Scheduled.
public class AuctionTimerTask {
    private final AuctionService auctionService;

    public AuctionTimerTask(AuctionService auctionService){
        this.auctionService=auctionService;
    }
    @Scheduled(fixedDelay = 1000)//mỗi 1 giây sau lần chạy trước
    public void tick(){
        LocalDateTime now=LocalDateTime.now();
        List<Auction> all= auctionService.listAll();
        for(Auction a:all){
            tryStart(a,now);
            tryFinish(a,now);
        }
    }
    private void tryStart(Auction a, LocalDateTime now){
        if(!"OPEN".equals(a.getStatus())) return;
        if(a.getStartTime()==null || now.isBefore(a.getStartTime())) return;
        try{
            auctionService.start(a);
        }catch (RuntimeException e){
            System.out.println("[Timer] start lỗi " + a.getId() + ": " + e.getMessage());//1 phiên lỗi k chặn các phiên khác
        }
    }
    private void tryFinish(Auction a,LocalDateTime now){
        if(!"RUNNING".equals(a.getStatus())) return;
        LocalDateTime end=auctionService.getLive(a.getId()).map(LiveAuction::getEndTime).orElse(a.getEndTime());

        if(end==null || now.isBefore(end)) return;
        try{
            auctionService.finish(a);
        }catch (RuntimeException e){
            System.out.println("[Timer] finish lỗi " + a.getId() + ": " + e.getMessage());
        }
    }
}
