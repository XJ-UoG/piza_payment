package com.xj.payment_processor.service;

import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.xj.payment_processor.model.User;
import com.xj.payment_processor.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserService(UserRepository userRepository, RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    public User createUser(String username, Double balance) {
        User user = new User();
        user.setUsername(username);
        user.setBalance(balance);
        return userRepository.save(user);
    }

    public Double getUserBalance(Long userId){
        String redisKey = "user_balance:" + userId;
        Double balance = (Double) redisTemplate.opsForValue().get(redisKey);
        if (balance == null) {
            balance = userRepository.findById(userId)
            .map(User::getBalance)
            .orElseThrow(() -> new RuntimeException("User not found"));
        }
        return balance;
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
