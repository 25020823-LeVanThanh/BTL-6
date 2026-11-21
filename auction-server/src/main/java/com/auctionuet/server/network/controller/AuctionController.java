/*
AuctionController là cửa vào cho mọi yêu cầu liên quan đến phiên đấu giá. Nó nhận request từ bên ngoài,
gọi AuctionService, rồi trả kết quả
*/

package com.auctionuet.server.network.controller;

import com.auctionuet.server.model.Auction;
import com.auctionuet.server.service.AuctionService;
import org.springframework.http.HttpStatus;//HttpStatus: enum các mã trạng thái HTTP (200, 201, 400, 404...).
import org.springframework.http.ResponseEntity;//ResponseEntity: đối tượng đại diện toàn bộ phản hồi HTTP (mã trạng thái + nội dung).
import org.springframework.web.bind.annotation.*;//...annotation.*: import tất cả annotation web (@RestController, @GetMapping, @PostMapping...)

import java.util.List;
import java.util.Map;

@RestController//báo: “class này nhận request HTTP, và giá trị method trả về sẽ được chuyển thành JSON gửi cho client”.
@RequestMapping("/api/auctions")//tiền tố đường dẫn chung. Mọi endpoint bên trong đều bắt đầu bằng /api/auctions.
public class AuctionController {
    private final AuctionService auctionService;
    public AuctionController(AuctionService auctionService){
        this.auctionService=auctionService;
    }
    @PostMapping// tạo phiên mới(Post tói /api/auctions
    public ResponseEntity<?> create (@RequestBody Auction auction){
        /*
        @RequestBody Auction auction
        Annotation này bảo Spring: “lấy dữ liệu client gửi lên và biến nó thành đối tượng Java Auction cho tôi.”
        */
        try{
            Auction created = auctionService.create(auction);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));
        }
    }
    @GetMapping
    public List<Auction> listAll(){
        return auctionService.listAll();
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id){
        return auctionService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error","Không tìm thấy phiên")));
    }
    @GetMapping("/{id}/live")
    public ResponseEntity<?> getLive(@PathVariable String id){
        return auctionService.getLive(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error","Phiên không đang live")));
    }
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String id){
        try{
            Auction a=auctionService.findById(id)
                    .orElseThrow(()->new IllegalArgumentException("Không tìm thấy phiên"));
            auctionService.cancel(a);
            return ResponseEntity.ok(Map.of("message","Đã hủy phiên live","id",id));
        }catch(IllegalArgumentException | IllegalStateException e){
            return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));
        }
    }
}