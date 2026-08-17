package com.Gokul.daily_help_exchange.Service;

import com.Gokul.daily_help_exchange.Dto.LoginRequest;
import com.Gokul.daily_help_exchange.Dto.LoginResponse;
import com.Gokul.daily_help_exchange.Dto.RegisterRequest;
import com.Gokul.daily_help_exchange.Model.User;
import com.Gokul.daily_help_exchange.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(RegisterRequest request) {
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new IllegalStateException("Phone number is already registered.");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getName(), request.getPhone(), hashedPassword);
        return userRepository.save(user);
    }

    public LoginResponse loginUser(LoginRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new IllegalStateException("Invalid phone number or password."));

        boolean validPassword;
        if (user.getPassword().startsWith("$2")) {
            validPassword = passwordEncoder.matches(request.getPassword(), user.getPassword());
        } else {
            validPassword = user.getPassword().equals(legacySha256(request.getPassword()));
            if (validPassword) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);
            }
        }

        if (!validPassword) {
            throw new IllegalStateException("Invalid phone number or password.");
        }

        return LoginResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .message("Login successful")
                .build();
    }

    private String legacySha256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte value : encodedHash) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not verify password.", exception);
        }
    }

}
