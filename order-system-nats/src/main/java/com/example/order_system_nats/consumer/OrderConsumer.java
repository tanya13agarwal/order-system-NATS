// Consumer (JetStream Durable Consumer)

package com.example.order_system_nats.consumer;

import io.nats.client.*;
import io.nats.client.api.StreamConfiguration;

import org.springframework.stereotype.Component;

import com.example.order_system_nats.model.OrderDTO;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.ObjectMapper;

@Component
public class OrderConsumer {

    private final Connection connection;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderConsumer(Connection connection) {
        this.connection = connection;
    }

    // @PostConstruct
    // public void startConsumer() throws Exception {

    //     // jetstream setup
    //     JetStream js = connection.jetStream();
    //     JetStreamManagement jsm = connection.jetStreamManagement();

    //     // Ensure stream exists
    //     try {
    //         jsm.getStreamInfo("ORDERS");
    //     } catch (Exception e) {
    //         jsm.addStream(StreamConfiguration.builder()
    //                 .name("ORDERS")
    //                 .subjects("orders.*")
    //                 .build());
    //     }

    //     // Create durable consumer by name:- order-processor
    //     PullSubscribeOptions options = PullSubscribeOptions.builder()
    //             .durable("order-processor")
    //             .build();

    //     JetStreamSubscription subscription =
    //             js.subscribe("orders.*", options);

    //     new Thread(() -> {
    //         while (true) {
    //             try {
    //                 // pull subscription like kafka.nextMessage
    //                 Message msg = subscription.nextMessage(1000);
    //                 if (msg != null) {
    //                     String data = new String(msg.getData());
    //                     OrderDTO order = objectMapper.readValue(data, OrderDTO.class);
    //                     System.out.println("Consumed Order: " + order);
    //                     // ACK - Marks message processed
    //                     // if no ack, JetStream → redelivers message
    //                     msg.ack();
    //                 }
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     }).start();
    // }

    @PostConstruct
public void startConsumer() throws Exception {

    System.out.println("🔥 Consumer starting...");  // ADD THIS

    JetStream js = connection.jetStream();

    PullSubscribeOptions options = PullSubscribeOptions.builder()
            .durable("order-processor")
            .build();

    JetStreamSubscription subscription =
            js.subscribe("orders.*", options);

    System.out.println("✅ Subscription created"); // ADD THIS

    new Thread(() -> {
        System.out.println("🚀 Consumer thread started"); // ADD THIS

        // while (true) {
        //     try {
        //         Message msg = subscription.nextMessage(1000);

        //         if (msg != null) {

        //             System.out.println("📩 Message received raw"); // ADD

        //             String data = new String(msg.getData());

        //             System.out.println("📩 Raw Data: " + data); // ADD

        //             OrderDTO order = objectMapper.readValue(data, OrderDTO.class);

        //             System.out.println("✅ Consumed Order: " + order);

        //             msg.ack();
        //         }

        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        // }
        while (true) {
    try {
        subscription.pull(10); // request 10 messages

        Message msg;

        while ((msg = subscription.nextMessage(1000)) != null) {

            System.out.println("📩 Message received raw");

            String data = new String(msg.getData());

            System.out.println("📩 Raw Data: " + data);

            OrderDTO order = objectMapper.readValue(data, OrderDTO.class);

            System.out.println("✅ Consumed Order: " + order);

            msg.ack();
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    }).start();
}
}
