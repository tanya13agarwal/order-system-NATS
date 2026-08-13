package com.example.order_system_nats.producer;

import io.nats.client.Connection;
import io.nats.client.JetStream;

import org.springframework.stereotype.Component;

import com.example.order_system_nats.model.OrderDTO;

import tools.jackson.databind.ObjectMapper;

@Component
public class OrderPublisher {

    private final Connection connection;
    private final ObjectMapper objectMapper;

    public OrderPublisher(Connection connection) {
        this.connection = connection;
        this.objectMapper = new ObjectMapper();
    }

    public void publishOrder(OrderDTO order) throws Exception {

        String subject = "orders.created";
        String message = objectMapper.writeValueAsString(order);

        // sends messages to nats server on the specified subject
        // connection.publish(subject, message.getBytes());

        JetStream js = connection.jetStream();

        js.publish(subject, message.getBytes());

        System.out.println("Published Order: " + message);
    }
}