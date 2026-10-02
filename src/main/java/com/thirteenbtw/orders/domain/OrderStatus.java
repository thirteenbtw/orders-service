package com.thirteenbtw.orders.domain;

import java.util.Set;

public enum OrderStatus {
    NEW, PAID, SHIPPED, CANCELLED;

    public Set<OrderStatus> next() {
        return switch (this) {
            case NEW -> Set.of(PAID, CANCELLED);
            case PAID -> Set.of(SHIPPED, CANCELLED);
            case SHIPPED, CANCELLED -> Set.of();
        };
    }

    public boolean canMoveTo(OrderStatus target) {
        return next().contains(target);
    }
}
