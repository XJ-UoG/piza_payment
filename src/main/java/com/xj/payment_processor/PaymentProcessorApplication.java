package com.xj.payment_processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class PaymentProcessorApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load(); // Load environment variables from .env
        SpringApplication.run(PaymentProcessorApplication.class, args);
    }
}
