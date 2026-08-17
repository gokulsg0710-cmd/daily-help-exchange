package com.Gokul.daily_help_exchange.Controller;

import com.Gokul.daily_help_exchange.Dto.LoginRequest;
import com.Gokul.daily_help_exchange.Dto.LoginResponse;
import com.Gokul.daily_help_exchange.Dto.RegisterRequest;
import com.Gokul.daily_help_exchange.Model.User;
import com.Gokul.daily_help_exchange.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerUser(request);
        return Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "phone", user.getPhone(),
                "message", "Registration successful"
        );
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        LoginResponse response = userService.loginUser(request);
        session.setAttribute("userId", response.getId());
        session.setAttribute("userName", response.getName());
        session.setAttribute("userPhone", response.getPhone());
        return response;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in first."));
        }

        return ResponseEntity.ok(Map.of(
                "id", userId,
                "name", session.getAttribute("userName"),
                "phone", session.getAttribute("userPhone")
        ));
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        session.invalidate();
        return Map.of("message", "Logout successful");
    }
}
