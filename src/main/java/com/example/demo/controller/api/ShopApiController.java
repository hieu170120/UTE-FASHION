package com.example.demo.controller.api;

import com.example.demo.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopApiController {

    private final ShopService shopService;

    /**
     * API endpoint để lấy ID của vendor (chủ shop) từ shopId.
     * Cần thiết cho việc kết nối WebSocket phía client.
     * @param shopId ID của cửa hàng
     * @return ID của user là chủ cửa hàng (vendorId)
     */
    @GetMapping("/{shopId}/vendor-id")
    public ResponseEntity<Integer> getVendorIdByShopId(@PathVariable Integer shopId) {
        try {
            Integer vendorId = shopService.getVendorIdByShopId(shopId);
            return ResponseEntity.ok(vendorId);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
