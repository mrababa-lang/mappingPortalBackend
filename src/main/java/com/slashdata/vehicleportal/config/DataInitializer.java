package com.slashdata.vehicleportal.config;

import com.slashdata.vehicleportal.entity.Role;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

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
        if (userRepository.count() == 0) {
            User adminUser = new User();
            adminUser.setName("Admin");
            adminUser.setEmail("admin@firsttech.ae");
            adminUser.setPassword(passwordEncoder.encode("password"));
            adminUser.setRole(Role.ADMIN);
            userRepository.save(adminUser);
        }
    }
}
