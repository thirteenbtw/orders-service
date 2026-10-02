package com.thirteenbtw.orders.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OrderStatusTest {
    @Test
    void allowedTransitions() {
        assertTrue(OrderStatus.NEW.canMoveTo(OrderStatus.PAID));
        assertTrue(OrderStatus.PAID.canMoveTo(OrderStatus.SHIPPED));
        assertTrue(OrderStatus.PAID.canMoveTo(OrderStatus.CANCELLED));
    }

    @Test
    void forbiddenTransitions() {
        assertFalse(OrderStatus.NEW.canMoveTo(OrderStatus.SHIPPED));
        assertFalse(OrderStatus.SHIPPED.canMoveTo(OrderStatus.CANCELLED));
        assertTrue(OrderStatus.CANCELLED.next().isEmpty());
    }
}
