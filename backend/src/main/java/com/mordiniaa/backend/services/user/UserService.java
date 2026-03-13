package com.mordiniaa.backend.services.user;

import com.mordiniaa.backend.exceptions.BadRequestException;
import com.mordiniaa.backend.models.auth.PasswordResetToken;
import com.mordiniaa.backend.models.user.DbUser;
import com.mordiniaa.backend.models.user.mysql.AppRole;
import com.mordiniaa.backend.models.user.mysql.Contact;
import com.mordiniaa.backend.models.user.mysql.User;
import com.mordiniaa.backend.repositories.mongo.user.UserRepresentationRepository;
import com.mordiniaa.backend.repositories.mysql.PasswordResetTokenRepository;
import com.mordiniaa.backend.repositories.mysql.UserRepository;
import com.mordiniaa.backend.request.auth.ResetPasswordTokenRequest;
import com.mordiniaa.backend.services.storage.profileImagesStorage.ImagesStorageService;
import com.mordiniaa.backend.utils.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRepresentationRepository userRepresentationRepository;
    private final MongoUserService mongoUserService;
    private final ImagesStorageService imagesStorageService;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User Not Found"));
    }

    public void addProfileImage(UUID userId, MultipartFile file) {

        mongoUserService.checkUserAvailability(userId);
        DbUser user = userRepresentationRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("User Not Found"));

        imagesStorageService.addProfileImage(user, file);
    }

    public void setDefaultProfileImage(UUID userId) {

        mongoUserService.checkUserAvailability(userId);
        DbUser user = userRepresentationRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("User Not Found"));

        imagesStorageService.setDefaultImage(user);
    }

    @Transactional
    public void generatePasswordResetToken(String username) {

        User user = userRepository.findUserByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new BadRequestException("User Not Found"));

        Contact userContactData = user.getContact();

        String email = userContactData.getEmail();

        UUID token = UUID.randomUUID();
        Instant expiryDate = Instant.now().plus(24, ChronoUnit.HOURS);

        PasswordResetToken resetToken = new PasswordResetToken(token, expiryDate, user);
        passwordResetTokenRepository.save(resetToken);

        //Frontend Does Not Exist Yet
        String resetUrl = "http://localhost:3000" + "/reset-password?token=" + token;

        emailService.sendPasswordResetEmail(email, resetUrl);
    }

    @Transactional
    public void resetPassword(ResetPasswordTokenRequest request) {

        String token = request.getToken();
        String newPassword = request.getNewPassword();

        UUID storedToken;
        try {
            storedToken = UUID.fromString(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Token");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findPasswordResetTokenByToken(storedToken)
                .orElseThrow(() -> new BadRequestException("Invalid Refresh Token"));

        if (resetToken.isUsed()) {
            throw new BadRequestException("Token Already Used");
        }

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BadRequestException("Token Expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    public User findNonDeletedUserAndAppRole(UUID userId, AppRole appRole) {
        return userRepository.findUserByUserIdAndDeletedFalseAndRole_AppRole(userId, appRole)
                .orElseThrow(() -> new BadRequestException("User Not Found"));
    }
}
