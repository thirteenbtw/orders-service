package com.thirteenbtw.orders.web;

import com.thirteenbtw.orders.dto.Dtos.*;
import com.thirteenbtw.orders.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService products;

    public ProductController(ProductService products) {
        this.products = products;
    }

    @GetMapping
    public List<ProductResponse> list() { return products.list(); }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) { return products.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse create(@Valid @RequestBody ProductRequest req) { return products.create(req); }
}
