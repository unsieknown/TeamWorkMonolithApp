package com.mordiniaa.backend.services.user;

import com.mordiniaa.backend.models.user.DbUser;
import com.mordiniaa.backend.models.user.mysql.AppRole;
import com.mordiniaa.backend.models.user.mysql.User;
import com.mordiniaa.backend.repositories.mongo.user.UserRepresentationRepository;
import com.mordiniaa.backend.repositories.mysql.UserRepository;
import com.mordiniaa.backend.services.storage.profileImagesStorage.ImagesStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRepresentationRepository userRepresentationRepository;
    private final MongoUserService mongoUserService;
    private final ImagesStorageService imagesStorageService;

    public User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(RuntimeException::new); // TODO: Change In Exceptions Section
    }

    public void addProfileImage(UUID userId, MultipartFile file) {

        mongoUserService.checkUserAvailability(userId);
        DbUser user = userRepresentationRepository.findByUserId(userId)
                .orElseThrow(RuntimeException::new); // TODO: Change In Exceptions Section

        imagesStorageService.addProfileImage(user, file);
    }

    public void setDefaultProfileImage(UUID userId) {

        mongoUserService.checkUserAvailability(userId);
        DbUser user = userRepresentationRepository.findByUserId(userId)
                .orElseThrow(RuntimeException::new); // TODO: Change In Exceptions Section

        imagesStorageService.setDefaultImage(user);
    }

    public User findNonDeletedUserAndAppRole(UUID userId, AppRole appRole) {
        return userRepository.findUserByUserIdAndDeletedFalseAndRole_AppRole(userId, appRole)
                .orElseThrow(RuntimeException::new); // TODO: Change In Exceptions Section
    }
}
