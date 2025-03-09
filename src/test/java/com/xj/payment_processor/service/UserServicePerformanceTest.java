package com.xj.payment_processor.service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.xj.payment_processor.model.User;
import com.xj.payment_processor.repository.UserRepository;

@SpringBootTest
public class UserServicePerformanceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Create 50 test users
        for (int i = 1; i <= 50; i++) {
            String username = "testUser" + i;
            if (userRepository.findByUsername(username).isEmpty()) {
                User newUser = new User(username, 1000.0 + i);
                userRepository.save(newUser);
            }
        }
        
        // Warm up Redis cache
        for (int i = 1; i <= 50; i++) {
            String username = "testUser" + i;
            userService.getUserBalance(username);
        }
    }

    @Test
    void testPerformance() throws InterruptedException {
        int numThreads = 1000;
        int requestsPerThread = 50;
        Random random = new Random();

        Thread[] threads = new Thread[numThreads];
        long redisStartTime = System.nanoTime();

        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    String randomUsername = "testUser" + (random.nextInt(50) + 1);
                    userService.getUserBalance(randomUsername);
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        long redisEndTime = System.nanoTime();
        long redisDurationMs = TimeUnit.NANOSECONDS.toMillis(redisEndTime - redisStartTime);
        System.out.println("Redis Query Time (Cached): " + redisDurationMs + " ms");

        threads = new Thread[numThreads];
        long dbStartTime = System.nanoTime();

        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    String randomUsername = "testUser" + (random.nextInt(50) + 1);
                    userService.getUserBalanceFromDB(randomUsername);
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        long dbEndTime = System.nanoTime();
        long dbDurationMs = TimeUnit.NANOSECONDS.toMillis(dbEndTime - dbStartTime);
        System.out.println("DB Query Time: " + dbDurationMs + " ms");
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        for (int i = 1; i <= 50; i++) {
            String username = "testUser" + i;
            userRepository.findByUsername(username)
                .ifPresent(user -> userRepository.delete(user));
        }
    }
}