package com.spring_cloud.eureka.client.order.orders;

import com.spring_cloud.eureka.client.order.core.Order;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
    private Long orderId;
    private String status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private List<Long> orderItemIds;

    public OrderResponseDto(Order order) {
        this.orderId = order.getId();
        this.status = order.getStatus().name();
        this.createdAt = order.getCreatedAt();
        this.createdBy = order.getCreatedBy();
        this.updatedAt = order.getUpdatedAt();
        this.updatedBy = order.getUpdatedBy();
        this.orderItemIds = order.getOrderItemIds();
    }
}
