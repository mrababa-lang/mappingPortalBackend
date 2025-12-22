package com.slashdata.vehicleportal.service;

import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.repository.UserRepository;
import java.security.Principal;
import org.springframework.stereotype.Service;

@Service
public class UserLookupService {

    private final UserRepository userRepository;

    public UserLookupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByPrincipal(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return null;
        }
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }
}
