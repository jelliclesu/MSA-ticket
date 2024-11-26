package com.spring_cloud.eureka.client.order.orders;

import com.spring_cloud.eureka.client.order.core.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom{
    Optional<Order> findByIdAndDeletedAtIsNull(Long id);
}
