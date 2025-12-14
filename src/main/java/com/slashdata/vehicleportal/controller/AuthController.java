package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.dto.LoginRequest;
import com.slashdata.vehicleportal.dto.LoginResponse;
import com.slashdata.vehicleportal.dto.UserDto;
import com.slashdata.vehicleportal.repository.UserRepository;
import com.slashdata.vehicleportal.security.JwtService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        com.slashdata.vehicleportal.entity.User user = userRepository.findByEmail(principal.getUsername())
            .orElseThrow();
        user.setLastActive(LocalDateTime.now());
        userRepository.save(user);
        String token = jwtService.generateToken(principal);
        LoginResponse response = new LoginResponse(token, UserDto.from(user));
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
