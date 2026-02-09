package net.ikwa.techconnect.service;

import lombok.RequiredArgsConstructor;
import net.ikwa.techconnect.model.PromoterUserModel;
import net.ikwa.techconnect.repo.PromoterRegRepo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromoterLoginService {

    private final PromoterRegRepo promoterRegRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public PromoterUserModel authenticatePromoter(String email, String password) {

        PromoterUserModel promoter = promoterRegRepo.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(password, promoter.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return promoter;
    }
}
