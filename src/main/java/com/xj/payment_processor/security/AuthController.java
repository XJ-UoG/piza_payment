package com.xj.payment_processor.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xj.payment_processor.model.User;
import com.xj.payment_processor.service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    public AuthController(JwtUtil jwtUtil, AuthenticationManager authenticationManager, UserService userService, PasswordEncoder passwordEncoder){
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest){
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(),
                    authRequest.getPassword()
                )
            );
    
            UserDetails userDetails = userService.loadUserByUsername(authRequest.getUsername());

            final String jwt = jwtUtil.generateToken(userDetails.getUsername());
            return ResponseEntity.ok(new AuthResponse(jwt));
    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/register")   
    public ResponseEntity<?> registerUser(@RequestParam String username, @RequestParam String password, @RequestParam(defaultValue = "0.0") Double balance) {
        if (userService.getUserByUsername(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }
    
        // Encode the password
        String encodedPassword = passwordEncoder.encode(password);
    
        User user = userService.createUser(username, encodedPassword, balance);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}


class AuthRequest {
    private String username;
    private String password;
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
}

class AuthResponse {
    private String token;
    public AuthResponse(String token) { this.token = token; }
    public String getToken() {
        return token;
    }
}