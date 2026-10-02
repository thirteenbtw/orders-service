package com.thirteenbtw.orders.service;

import com.thirteenbtw.orders.domain.*;
import com.thirteenbtw.orders.dto.Dtos.*;
import com.thirteenbtw.orders.repo.OrderRepository;
import com.thirteenbtw.orders.repo.ProductRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final ProductRepository products;

    public OrderService(OrderRepository orders, ProductRepository products) {
        this.orders = orders;
        this.products = products;
    }

    @Transactional
    public OrderResponse place(Long userId, CreateOrderRequest req) {
        Map<Long, Integer> wanted = new LinkedHashMap<>();
        req.items().forEach(l -> wanted.merge(l.productId(), l.quantity(), Integer::sum));

        Map<Long, Product> locked = new LinkedHashMap<>();
        products.lockAllByIdIn(wanted.keySet()).forEach(p -> locked.put(p.getId(), p));

        Order order = new Order(userId);
        for (var e : wanted.entrySet()) {
            Product p = locked.get(e.getKey());
            if (p == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Товар " + e.getKey() + " не найден");
            }
            if (p.getStock() < e.getValue()) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Недостаточно на складе: " + p.getName() + " (есть " + p.getStock() + ")");
            }
            p.decreaseStock(e.getValue());
            order.addItem(p, e.getValue());
        }
        return OrderResponse.of(orders.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listFor(Long userId) {
        return orders.findByUserIdOrderByCreatedAtDesc(userId).stream().map(OrderResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long userId, boolean admin, Long id) {
        return OrderResponse.of(load(userId, admin, id));
    }

    @Transactional
    public OrderResponse changeStatus(Long userId, boolean admin, Long id, OrderStatus target) {
        Order o = load(userId, admin, id);
        if (!admin && target != OrderStatus.CANCELLED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Недостаточно прав");
        }
        if (!o.getStatus().canMoveTo(target)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Переход " + o.getStatus() + " -> " + target + " недопустим");
        }
        if (target == OrderStatus.CANCELLED) {
            restock(o);
        }
        o.changeStatus(target);
        return OrderResponse.of(o);
    }

    private void restock(Order o) {
        List<Long> ids = o.getItems().stream().map(OrderItem::getProductId).distinct().sorted().toList();
        Map<Long, Product> locked = new LinkedHashMap<>();
        products.lockAllByIdIn(ids).forEach(p -> locked.put(p.getId(), p));
        o.getItems().forEach(i -> locked.get(i.getProductId()).increaseStock(i.getQuantity()));
    }

    private Order load(Long userId, boolean admin, Long id) {
        Order o = orders.findWithItemsById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Заказ не найден"));
        if (!admin && !o.getUserId().equals(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Заказ не найден");
        }
        return o;
    }
}
