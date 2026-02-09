package net.ikwa.techconnect.service;

import lombok.RequiredArgsConstructor;
import net.ikwa.techconnect.model.CreatorUserModel;
import net.ikwa.techconnect.repo.TechRegRepo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TechLoginService {

    private final TechRegRepo techRegRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public CreatorUserModel authenticateCreator(String email, String password) {

        CreatorUserModel creator = techRegRepo.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(password, creator.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return creator;
    }
}
