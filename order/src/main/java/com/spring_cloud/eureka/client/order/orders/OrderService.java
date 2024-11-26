package com.spring_cloud.eureka.client.order.orders;

import com.spring_cloud.eureka.client.order.core.Order;
import com.spring_cloud.eureka.client.order.core.ProductClient;
import com.spring_cloud.eureka.client.order.core.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    // 생성
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto, String userId) {
        // product 존재 유무, 수량 확인
        for (Long productId : requestDto.getOrderItemIds()) {
            ProductResponseDto product = productClient.getProductById(productId);
            log.info("############################ Product 수량 확인 : " + product.getQuantity());
            if (product.getQuantity() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product with ID " + productId + " is out of stock.");
            }
        }

        // 주문 생성 시, product 수량 1 감소
        for (Long productId : requestDto.getOrderItemIds()) {
            productClient.reduceProductQuantity(productId, 1);
        }

        Order order = Order.createOrder(requestDto.getOrderItemIds(), userId);
        Order savedOrder = orderRepository.save(order);
        return new OrderResponseDto(savedOrder);
    }

    // 단건 조회
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다."));

        return new OrderResponseDto(order);
    }

    // 목록 조회
    public Page<OrderResponseDto> getOrders(OrderSearchDto searchDto, String userId, String role, Pageable pageable) {
        return orderRepository.searchOrder(searchDto, userId, role, pageable);
    }

    // 수정
    @Transactional
    public OrderResponseDto updateOrder(Long id, OrderRequestDto requestDto, String userId) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다."));
        order.update(requestDto, userId);

        return new OrderResponseDto(order);
    }

    // 삭제
    @Transactional
    public void deleteOrder(Long id, String userId) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다."));
        order.delete(userId);
    }
}
