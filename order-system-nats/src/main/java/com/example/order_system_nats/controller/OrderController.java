// HTTP request → publish to NATS

package com.example.order_system_nats.controller;

import org.springframework.web.bind.annotation.*;

import com.example.order_system_nats.model.OrderDTO;
import com.example.order_system_nats.producer.OrderPublisher;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderPublisher publisher;

    public OrderController(OrderPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping
    public String createOrder(@RequestBody OrderDTO order) throws Exception {
        publisher.publishOrder(order);
        return "Order published!";
    }
}