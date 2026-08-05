package com.leandrosouza.cdc_orders.repository;

import com.leandrosouza.cdc_orders.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}