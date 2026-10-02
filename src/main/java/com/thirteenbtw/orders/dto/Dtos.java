package com.thirteenbtw.orders.dto;

import com.thirteenbtw.orders.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class Dtos {
    private Dtos() {}

    public record RegisterRequest(@Email @NotBlank String email, @NotBlank @Size(min = 8, max = 72) String password) {}
    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record TokenResponse(String token) {}

    public record ProductRequest(@NotBlank @Size(max = 200) String name,
                                 @NotNull @DecimalMin("0.00") BigDecimal price,
                                 @Min(0) int stock) {}
    public record ProductResponse(Long id, String name, BigDecimal price, int stock) {
        public static ProductResponse of(Product p) {
            return new ProductResponse(p.getId(), p.getName(), p.getPrice(), p.getStock());
        }
    }

    public record OrderLineRequest(@NotNull Long productId, @Min(1) @Max(1000) int quantity) {}
    public record CreateOrderRequest(@NotEmpty @Valid List<OrderLineRequest> items) {}
    public record StatusRequest(@NotNull OrderStatus status) {}

    public record OrderItemResponse(Long productId, String productName, BigDecimal unitPrice, int quantity) {}
    public record OrderResponse(Long id, OrderStatus status, BigDecimal total, Instant createdAt,
                                List<OrderItemResponse> items) {
        public static OrderResponse of(Order o) {
            return new OrderResponse(o.getId(), o.getStatus(), o.getTotal(), o.getCreatedAt(),
                    o.getItems().stream()
                            .map(i -> new OrderItemResponse(i.getProductId(), i.getProductName(),
                                    i.getUnitPrice(), i.getQuantity()))
                            .toList());
        }
    }
}
