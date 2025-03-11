package com.xj.payment_processor.service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.xj.payment_processor.model.User;
import com.xj.payment_processor.repository.UserRepository;

@Service
public class UserService implements UserDetailsService{
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserService(UserRepository userRepository, RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    public User createUser(String username, String password, Double balance) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setBalance(balance);
        return userRepository.save(user);
    }

    public Double getUserBalance(Long userId) {
        String redisKey = "user_balance:" + userId;
        String balanceStr = (String) redisTemplate.opsForValue().get(redisKey);
        if (balanceStr != null) {
            return Double.parseDouble(balanceStr);
        }
        Double balance = getUserBalanceFromDB(userId);
        redisTemplate.opsForValue().set(redisKey, String.valueOf(balance), 5, TimeUnit.MINUTES);

        return balance;
    }

    public Double getUserBalance(String username) {
        String redisKey = "user_balance:" + username;
        String balanceStr = (String) redisTemplate.opsForValue().get(redisKey);
        if (balanceStr != null) {
            return Double.parseDouble(balanceStr);
        }
        Double balance = getUserBalanceFromDB(username);
        redisTemplate.opsForValue().set(redisKey, String.valueOf(balance), 5, TimeUnit.MINUTES);

        return balance;
    }

    public Double getUserBalanceFromDB(Long userId) {
        return userRepository.findById(userId)
                .map(User::getBalance)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Double getUserBalanceFromDB(String username) {
        return userRepository.findByUsername(username)
        .map(User::getBalance)
        .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
