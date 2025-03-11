package com.xj.payment_processor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.xj.payment_processor.producer.TestTopicProducer;

@RestController
@RequestMapping("/kafka")
public class KafkaTestController {
    private TestTopicProducer producer;

    public KafkaTestController(TestTopicProducer producer) {
        this.producer = producer;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestParam String message) {
        try {
            producer.sendMessage(message);
            return ResponseEntity.ok("Message sent: " + message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
