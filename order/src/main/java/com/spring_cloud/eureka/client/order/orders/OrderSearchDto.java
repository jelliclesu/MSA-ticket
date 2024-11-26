package com.spring_cloud.eureka.client.order.orders;

import com.spring_cloud.eureka.client.order.core.OrderStatus;
import lombok.Data;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Data
public class OrderSearchDto {
    private OrderStatus status;
    private List<Long> orderItemIds;
    private String sortBy;
    private Pageable pageable;
}