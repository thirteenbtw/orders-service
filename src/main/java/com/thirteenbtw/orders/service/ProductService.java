package com.thirteenbtw.orders.service;

import com.thirteenbtw.orders.domain.Product;
import com.thirteenbtw.orders.dto.Dtos.*;
import com.thirteenbtw.orders.repo.ProductRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list() {
        return products.findAll().stream().map(ProductResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return products.findById(id).map(ProductResponse::of)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Товар не найден"));
    }

    @Transactional
    public ProductResponse create(ProductRequest r) {
        return ProductResponse.of(products.save(new Product(r.name(), r.price(), r.stock())));
    }
}
