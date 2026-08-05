package com.leandrosouza.cdc_orders.controller;

import com.leandrosouza.cdc_orders.domain.Order;
import com.leandrosouza.cdc_orders.domain.OrderStatus;
import com.leandrosouza.cdc_orders.repository.OrderRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository repository;

    public OrderController(OrderRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Order create(@RequestBody Order order) {
        order.setStatus(OrderStatus.CREATED);
        return repository.save(order);
    }
}