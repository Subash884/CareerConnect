package com.careerconnect.controller;

import com.careerconnect.dto.request.LoginRequest;
import com.careerconnect.dto.request.RegisterRequest;
import com.careerconnect.dto.response.ApiResponse;
import com.careerconnect.dto.response.JwtAuthResponse;
import com.careerconnect.entity.Role;
import com.careerconnect.entity.User;
import com.careerconnect.repository.UserRepository;
import com.careerconnect.security.CustomUserDetailsService;
import com.careerconnect.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==========================================
    // 1. REGISTRATION ENDPOINT
    // ==========================================
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@RequestBody RegisterRequest registerRequest) {

        // Check if email is already taken
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return new ResponseEntity<>(new ApiResponse(false, "Email is already taken!"), HttpStatus.BAD_REQUEST);
        }

        // Create new user entity
        User user = new User();
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        // Hash the password before saving!
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // Safely convert the string role into our Enum
        try {
            user.setRole(Role.valueOf(registerRequest.getRole().toUpperCase()));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse(false, "Invalid Role! Use ROLE_SEEKER or ROLE_EMPLOYER"), HttpStatus.BAD_REQUEST);
        }

        userRepository.save(user);

        return new ResponseEntity<>(new ApiResponse(true, "User registered successfully!"), HttpStatus.CREATED);
    }

    // ==========================================
    // 2. LOGIN ENDPOINT
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> loginUser(@RequestBody LoginRequest loginRequest) {

        // This will automatically check the database and verify the hashed password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        // If the code reaches here, the user is authenticated!
        // Now, generate the JWT token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);

        // Return the token in JSON format
        return ResponseEntity.ok(new JwtAuthResponse(jwt));
    }
}