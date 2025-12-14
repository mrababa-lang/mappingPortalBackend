package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ApiResponse<List<User>> findAll() {
        return ApiResponse.of(userRepository.findAll());
    }

    @PostMapping
    public ApiResponse<User> create(@Valid @RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return ApiResponse.of(userRepository.save(user));
    }

    @PutMapping("/{id}")
    public ApiResponse<User> update(@PathVariable String id, @Valid @RequestBody User user) {
        User existing = userRepository.findById(id).orElseThrow();
        existing.setName(user.getName());
        existing.setEmail(user.getEmail());
        existing.setRole(user.getRole());
        if (user.getPassword() != null) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        existing.setStatus(user.getStatus());
        return ApiResponse.of(userRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        User existing = userRepository.findById(id).orElseThrow();
        existing.setStatus("Inactive");
        userRepository.save(existing);
        return ResponseEntity.noContent().build();
    }
}
