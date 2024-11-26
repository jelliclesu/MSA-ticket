package com.spring_cloud.eureka.client.product.products;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 생성
    @PostMapping
    public ProductResponseDto createProduct(@RequestBody ProductRequestDto requestDto,
                                            @RequestHeader(value = "X-User-Id", required = true) String userId,
                                            @RequestHeader(value = "X-Role", required = true) String role) {
        if (!role.equals("MANAGER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "매니저 권한이 아닙니다.");
        }
        return productService.createProduct(requestDto, userId);
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ProductResponseDto getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    // 목록 조회
    @GetMapping
    public Page<ProductResponseDto> getProducts(ProductSearchDto searchDto, Pageable pageable) {
        return productService.getProducts(searchDto, pageable);
    }

    // 수정
    @PutMapping("/{id}")
    public ProductResponseDto updateProduct(@PathVariable Long id,
                                            @RequestBody ProductRequestDto requestDto,
                                            @RequestHeader(value = "X-User-Id", required = true) String userId) {
        return productService.updateProduct(id, requestDto, userId);
    }

    // 삭제
    @DeleteMapping("/{id}")
    public void  deleteProduct(@PathVariable Long id,
                               @RequestHeader(value = "X-User-Id", required = true) String userId) {
        productService.deleteProduct(id, userId);
    }

    // 수량 감소
    @GetMapping("/{id}/reduceQuantity")
    public void reduceProductQuantity(@PathVariable Long id, @RequestParam int quantity) {
        productService.reduceProductQuantity(id, quantity);
    }
}
