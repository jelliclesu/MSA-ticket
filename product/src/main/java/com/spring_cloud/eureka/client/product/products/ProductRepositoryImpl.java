package com.spring_cloud.eureka.client.product.products;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring_cloud.eureka.client.product.core.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.spring_cloud.eureka.client.product.core.QProduct.product;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductResponseDto> searchProducts(ProductSearchDto searchDto, Pageable pageable) {
        // Pageable 객체에서 정렬 조건 추출
        List<OrderSpecifier<?>> orders = getAllOrderSpecifiers(pageable);

        // QueryDSL 로 동적 검색 수행
        QueryResults<Product> results = queryFactory
                .selectFrom(product)
                .where(
                        nameContains(searchDto.getName()),              // 이름 검색 조건
                        descriptionContains(searchDto.getDescription()), // 설명 검색 조건
                        priceBetween(searchDto.getMinPrice(), searchDto.getMaxPrice()), // 가격 범위 조건
                        quantityBetween(searchDto.getMinQuantity(), searchDto.getMaxQuantity()), // 수량 범위 조건
                        isNotDeleted()
                )
                .orderBy(orders.toArray(new OrderSpecifier[0])) // 정렬 조건 추가
                .offset(pageable.getOffset())                 // 시작 위치 설정
                .limit(pageable.getPageSize())                // 페이징 크기 설정
                .fetchResults();                              // 쿼리 실행

        // 검색 결과를 DTO로 변환
        List<ProductResponseDto> content = results.getResults().stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());

        // 총 검색된 데이터 수
        long total = results.getTotal();

        // Page 객체로 결과 반환
        return new PageImpl<>(content, pageable, total);
    }

    // 삭제되지 않은 데이터 조건 (deletedAt이 null)
    private BooleanExpression isNotDeleted() {
        return product.deletedAt.isNull();
    }

    // 이름 포함 조건
    private BooleanExpression nameContains(String name) {
        return name != null ? product.name.containsIgnoreCase(name) : null;
    }

    // 설명 포함 조건
    private BooleanExpression descriptionContains(String description) {
        return description != null ? product.description.containsIgnoreCase(description) : null;
    }

    // 가격 범위 조건
    private BooleanExpression priceBetween(Double minPrice, Double maxPrice) {
        if (minPrice != null && maxPrice != null) {
            return product.price.between(minPrice, maxPrice);
        } else if (minPrice != null) {
            return product.price.goe(minPrice);
        } else if (maxPrice != null) {
            return product.price.loe(maxPrice);
        } else {
            return null;
        }
    }

    // 수량 범위 조건
    private BooleanExpression quantityBetween(Integer minQuantity, Integer maxQuantity) {
        if (minQuantity != null && maxQuantity != null) {
            return product.quantity.between(minQuantity, maxQuantity);
        } else if (minQuantity != null) {
            return product.quantity.goe(minQuantity);
        } else if (maxQuantity != null) {
            return product.quantity.loe(maxQuantity);
        } else {
            return null;
        }
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
                        orders.add(new OrderSpecifier<>(direction, product.createdAt));
                        break;
                    case "price":
                        orders.add(new OrderSpecifier<>(direction, product.price));
                        break;
                    case "quantity":
                        orders.add(new OrderSpecifier<>(direction, product.quantity));
                        break;
                    default:
                        break;
                }
            }
        }

        return orders;
    }
}
