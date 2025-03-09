package com.xj.payment_processor.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.xj.payment_processor.model.User;
import com.xj.payment_processor.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String username, Double balance) {
        User user = new User();
        user.setUsername(username);
        user.setBalance(balance);
        return userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
