package com.slashdata.vehicleportal.config;

import com.slashdata.vehicleportal.entity.Role;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String adminEmail = "admin@firsttech.ae";

        if (userRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        User adminUser = new User();
        adminUser.setName("Admin");
        adminUser.setFullName("Admin");
        adminUser.setEmail(adminEmail);
        adminUser.setPasswordUnhashed("Admin@123");
        adminUser.setPassword(passwordEncoder.encode(adminUser.getPasswordUnhashed()));
        adminUser.setRole(Role.ADMIN);
        adminUser.setStatus("ACTIVE");
        adminUser.setLastActive(now);
        adminUser.setCreatedAt(now);
        userRepository.save(adminUser);
    }
}
