CS6650 Assignment 1 - Ski Lift Ride Recording System

Author: Miao Xu
GitHub Repository: https://github.com/miaoxu0414/6650

Project Overview

This project implements a scalable distributed system to record ski lift ride events. It consists of:
• A server running on AWS EC2 (Java Servlet on Tomcat).
• A multi-threaded client (Client Part 1) for sending requests.
• An optimized client (Client Part 2) with performance tracking.

Server Setup 1. Deploy the WAR file
Copy the server/Assignment1.war file to Tomcat’s webapps directory on the AWS EC2 instance. 2. Start Tomcat

sudo systemctl restart tomcat

    3.	API Endpoint

The API is accessible at:

http://your-ec2-ip:8080/Assignment1/skiers

    4.	Test the Server

Use Postman or run the following curl command:

curl -X POST http://your-ec2-ip:8080/Assignment1/skiers \
 -H "Content-Type: application/json" \
 -d '{"skierID":123, "resortID":1, "liftID":5, "seasonID":2024, "dayID":1, "time":250}'

Running Clients

Client 1

    1.	Navigate to the client1/src/ directory and compile:

javac org/example/client1/Client1.java

    2.	Run the client:

java org.example.client1.Client1

    3.	Outputs: Total requests sent, wall time, throughput.

Client 2

    1.	Navigate to the client2/src/ directory and compile:

javac org/example/client2/Client2.java

    2.	Run the client:

java org.example.client2.Client2

    3.	Outputs: Mean, median, P99, max response time, throughput.

Performance Analysis

    •	Client 1 Throughput: X requests/sec

    •	Client 2 Throughput: Y requests/sec (should be within 5% of Client 1)

• Throughput graph included: screenshots/throughput_plot.png
