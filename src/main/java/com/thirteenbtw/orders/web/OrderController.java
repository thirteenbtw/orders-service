package com.thirteenbtw.orders.web;

import com.thirteenbtw.orders.dto.Dtos.*;
import com.thirteenbtw.orders.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@AuthenticationPrincipal Long userId, @Valid @RequestBody CreateOrderRequest req) {
        return orders.place(userId, req);
    }

    @GetMapping
    public List<OrderResponse> mine(@AuthenticationPrincipal Long userId) {
        return orders.listFor(userId);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@AuthenticationPrincipal Long userId, Authentication auth, @PathVariable Long id) {
        return orders.get(userId, isAdmin(auth), id);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse status(@AuthenticationPrincipal Long userId, Authentication auth,
                                @PathVariable Long id, @Valid @RequestBody StatusRequest req) {
        return orders.changeStatus(userId, isAdmin(auth), id, req.status());
    }

    private static boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
