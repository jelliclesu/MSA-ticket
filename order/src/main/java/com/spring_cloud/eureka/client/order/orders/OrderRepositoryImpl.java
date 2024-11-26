package com.spring_cloud.eureka.client.order.orders;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring_cloud.eureka.client.order.core.Order;
import com.spring_cloud.eureka.client.order.core.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.spring_cloud.eureka.client.order.core.QOrder.order;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<OrderResponseDto> searchOrder(OrderSearchDto searchDto, String userId, String role, Pageable pageable) {
        // Pageable 객체에서 정렬 조건 추출
        List<OrderSpecifier<?>> orders = getAllOrderSpecifiers(pageable);

        // QueryDSL 로 동적 검색 수행
        QueryResults<Order> results = queryFactory
                .selectFrom(order)
                .where(
                        statusEq(searchDto.getStatus()),
                        orderItemIdsIn(searchDto.getOrderItemIds()),
                        userCheck(role, userId),
                        isNotDeleted()
                )
                .orderBy(orders.toArray(new OrderSpecifier[0])) // 정렬 조건 추가
                .offset(pageable.getOffset())                 // 시작 위치 설정
                .limit(pageable.getPageSize())                // 페이징 크기 설정
                .fetchResults();                              // 쿼리 실행

        // 검색 결과를 DTO로 변환
        List<OrderResponseDto> content = results.getResults().stream()
                .map(OrderResponseDto::new)
                .collect(Collectors.toList());

        // 총 검색된 데이터 수
        long total = results.getTotal();

        // Page 객체로 결과 반환
        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression userCheck(String role, String userId) {
        return role.equals("MEMBER") ? order.createdBy.eq(userId) : null;
    }

    private BooleanExpression orderItemIdsIn(List<Long> orderItemIds) {
        return orderItemIds != null && !orderItemIds.isEmpty() ? order.orderItemIds.any().in(orderItemIds) : null;
    }

    private BooleanExpression statusEq(OrderStatus status) {
        return status != null ? order.status.eq(status) : null;
    }

    // 삭제되지 않은 데이터 조건 (deletedAt이 null)
    private BooleanExpression isNotDeleted() {
        return order.deletedAt.isNull();
    }

    // 정렬 조건 생성
    private List<OrderSpecifier<?>> getAllOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        // Pageable 의 정렬 조건을 QueryDSL 의 OrderSpecifier 로 변환
        if (pageable.getSort() != null) {
            for (Sort.Order sortOrder : pageable.getSort()) {
                // 정렬 방향 결정 (ASC/DESC)
                com.querydsl.core.types.Order direction = sortOrder.isAscending()
                        ? com.querydsl.core.types.Order.ASC
                        : com.querydsl.core.types.Order.DESC;

                // 정렬 필드에 따른 OrderSpecifier 추가
                switch (sortOrder.getProperty()) {
                    case "createdAt":
                        orders.add(new OrderSpecifier<>(direction, order.createdAt));
                        break;
                    case "status":
                        orders.add(new OrderSpecifier<>(direction, order.status));
                        break;
                    default:
                        break;
                }
            }
        }

        return orders;
    }
}
