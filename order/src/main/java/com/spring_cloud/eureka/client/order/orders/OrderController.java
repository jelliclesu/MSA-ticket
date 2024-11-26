package com.spring_cloud.eureka.client.order.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 생성
    @PostMapping
    public OrderResponseDto createOrder(@RequestBody OrderRequestDto requestDto,
                                        @RequestHeader("X-User-Id") String userId) {
        return orderService.createOrder(requestDto, userId);
    }
    // 단건 조회
    @GetMapping("/{id}")
    public OrderResponseDto getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    // 목록 조회
    @GetMapping
    public Page<OrderResponseDto> getOrders(OrderSearchDto searchDto,
                                            @RequestHeader("X-User-Id") String userId,
                                            @RequestHeader("X-Role") String role,
                                            Pageable pageable
                                            ) {
        if (!role.equals("MANAGER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "매니저 권한이 아닙니다.");
        }
        return orderService.getOrders(searchDto, userId, role, pageable);
    }

    // 수정
    @PutMapping("/{id}")
    public OrderResponseDto updateOrder(@PathVariable Long id,
                                        @RequestBody OrderRequestDto requestDto,
                                        @RequestHeader("X-User-Id") String userId) {
        return orderService.updateOrder(id, requestDto, userId);
    }

    // 삭제
    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id,
                            @RequestParam String userId) {
        orderService.deleteOrder(id, userId);
    }
}
