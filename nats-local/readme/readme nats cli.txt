PHASE 1 - NATS Server

1. Run NATS Server (Basic)
	- open powershell-> cd C:\Users\tanya.agarwal\Desktop\nats-local\bin
	- run-> .\nats-server.exe
	NATS server on port 4222, Any application can now connect to NATS (NATS basic PUB/SUB is running)
	
2. Create configuration file
	- create a nats.conf file in nats-local\config
		( client connections happen on port
		- jetstream enables persistence layer
		- store_dir is wehere data is stored on disk
		- http:8222 is Monitoring UI port )
		
3. Jestream enabled nats server connection
	- .\nats-server.exe -c ..\config\nats.conf

4. Open browser http://localhost:8222 and you will see connections, memory usage, server stats




PHASE 2 - NATS CLI
	- install nats cli and add nats.exe in C:\Users\tanya.agarwal\Desktop\nats-local\bin
	
(FIRST PUB/SUB EXAMPLE) - nats pub → sends message → server → delivers to subscriber
	1. Start NATS Server
			cd C:\Users\tanya.agarwal\Desktop\nats-local\bin
			.\nats-server.exe -c ..\config\nats.conf
	2. Run Subscriber (subscribe to subject = orders.created)
			cd C:\Users\tanya.agarwal\Desktop\nats-local\bin
			.\nats.exe sub orders.created
	3. Open Another Terminal → Publish
			cd C:\Users\tanya.agarwal\Desktop\nats-local\bin
			.\nats.exe pub orders.created "order-1"
This is core NATS (non-persistent), If subscriber is offline: Message is Lost

Wildcard Subscription - subjects hierarchy
	1. subscriber:-
		.\nats.exe sub orders.*
	2. Publisher:- 
		.\nats.exe pub orders.created "order-created"
		.\nats.exe pub orders.updated "order-updated"
Subscriber receives both. orders.* → matches all sub-subjects

Queue Groups (Load Balancing)
we simulate consumer groups (like Kafka) - Multiple consumers share the load
without queue group, A and B both will get messages; with queue group, load will get distributed between A and B
	1. run in both terminals(Make them consumers):-
		.\nats.exe sub orders.created --queue workers
	2. on a new terminal(Publisher), run:-
		cd C:\Users\tanya.agarwal\Desktop\nats-local\bin
		.\nats.exe pub orders.created "order-1"
		.\nats.exe pub orders.created "order-2"
		.\nats.exe pub orders.created "order-3"
		.\nats.exe pub orders.created "order-4"
Messages will be distributed, not duplicated. e.g., Consumer A has 3, Consumer B has 1 message.

Request / Reply (Microservices Style) - Service Communication
	Terminal 1 - Service that will provide service and reply with message 
		.\nats.exe reply order.service "Order processed"
	Terminal 2 - Client that will request service
		.\nats.exe reply order.service "Order processed"
Client → request → service → response




PHASE 3 - JetStream (Persistence + Reliability)
	( Kafka Topic ≈ JetStream Stream
	Kafka Offset ≈ JetStream Sequence + ACK )
Publisher → Stream (stored) → Consumer
	
	1. Create stream
		.\nats.exe stream add ORDERS
		answer questions like Subjects: orders.*
			Storage: file
			Replicas: 1
			Retention: limits
			Max messages: -1
			Max bytes: -1
			Max age: 0
	2. Publish Messages (NOW THEY ARE STORED)
		Run on same or new terminal 
			.\nats.exe pub orders.created "order-1"
			.\nats.exe pub orders.created "order-2"
			.\nats.exe pub orders.created "order-3"
	Publisher → NATS → JetStream → Stored in stream
	3. Verify messages stored or not
		.\nats.exe stream info ORDERS
	Output will be something with Messages: 3
	4. Create Consumer on a new terminal
		cd C:\Users\tanya.agarwal\Desktop\nats-local\bin
		.\nats.exe consumer add ORDERS
		Give inputs like:-
			Name: order-processor
			Delivery: pull
			Ack: explicit
	5. Consume messages on the same consumer terminal
		.\nats.exe consumer next ORDERS order-processor
	You will see the messages coming one by one on each command run.
	for ACK Messages, run:- .\nats.exe consumer next ORDERS order-processor --ack

Terminal 2 → Publish
      │
      ▼
NATS Server (Terminal 1)
      │
      ▼
JetStream Stream (stored)
      │
      ▼
Terminal 3 → Consumer pulls messages