package com.mordiniaa.backend.services.storage.profileImagesStorage;

import com.mordiniaa.backend.config.StorageProperties;
import com.mordiniaa.backend.events.user.events.UserProfileImageChangedEvent;
import com.mordiniaa.backend.models.file.imageStorage.ImageMetadata;
import com.mordiniaa.backend.models.user.DbUser;
import com.mordiniaa.backend.repositories.mongo.ImageMetadataRepository;
import com.mordiniaa.backend.repositories.mysql.UserRepository;
import com.mordiniaa.backend.services.storage.StorageProvider;
import com.mordiniaa.backend.utils.CloudStorageServiceUtils;
import com.mordiniaa.backend.utils.MongoIdUtils;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImagesStorageService {

    private final MongoIdUtils mongoIdUtils;
    private final ImageMetadataRepository imageMetadataRepository;
    private final StorageProvider storageProvider;
    private final StorageProperties storageProperties;
    private final UserRepository userRepository;
    private final CloudStorageServiceUtils cloudStorageServiceUtils;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ResponseEntity<StreamingResponseBody> getProfileImage(String key) {

        if (storageProperties.getProfileImages().getDefaultImageKey().equals(key))
            return defaultImage();

        ObjectId objectId = mongoIdUtils.getObjectId(key);

        ImageMetadata meta = imageMetadataRepository.findById(objectId)
                .orElse(null);

        if (meta == null)
            return defaultImage();

        StreamingResponseBody body = outputStream -> {
            try (InputStream in = storageProvider.downloadFile(
                    storageProperties.getProfileImages().getPath(),
                    meta.getStoredName()
            )) {
                in.transferTo(outputStream);
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/" + meta.getExtension()))
                .body(body);
    }

    @Transactional
    public void addProfileImage(DbUser user, MultipartFile file) {

        StorageProperties.ProfileImages profileImages = storageProperties.getProfileImages();
        String mimetype = baseImageValidation(file, profileImages.getMimeTypes());

        ImageMetadata metadata = imageMetadataRepository.findImageMetadataByOwnerId(user.getUserId())
                .orElse(null);
        if (metadata != null)
            imageMetadataRepository.deleteById(metadata.getId());

        String originalName = file.getOriginalFilename();
        String ext = switch (getFileExtension(mimetype)) {
            case "jpeg", "jpg" -> "jpg";
            default -> "png";
        };

        String newImageName = cloudStorageServiceUtils.buildStorageKey().concat(".".concat(ext));
        String profileImagesPath = profileImages.getPath();

        try {
            addImage(
                    profileImagesPath,
                    newImageName,
                    ext,
                    profileImages.getProfileWidth(),
                    profileImages.getProfileHeight(),
                    file
            );

            ImageMetadata savedMeta = imageMetadataRepository.save(ImageMetadata.builder()
                    .originalName(originalName)
                    .storedName(newImageName)
                    .extension(ext)
                    .ownerId(user.getUserId())
                    .size(file.getSize())
                    .build()
            );

            updateUserImageKey(user.getUserId(), savedMeta.getId().toHexString());
        } catch (Exception e) {
            if (metadata != null) {
                imageMetadataRepository.save(metadata);
                updateUserImageKey(user.getUserId(), metadata.getId().toHexString());
            } else {
                updateUserImageKey(user.getUserId(), profileImages.getDefaultImageKey());
            }
            throw new RuntimeException(e);
        }

        if (metadata != null) {
            storageProvider.delete(
                    profileImagesPath,
                    metadata.getStoredName()
            );
        }
    }

    public void addImage(String profileImagesPath, String storedName, String ext, int width, int height, MultipartFile file) {

        boolean uploaded = false;
        try (InputStream in = file.getInputStream()) {
            storageProvider.uploadImage(
                    profileImagesPath,
                    storedName,
                    ext,
                    width,
                    height,
                    in
            );
            uploaded = true;
        } catch (Exception e) {
            if (uploaded) {
                removeImage(profileImagesPath, storedName);
            }
            throw new RuntimeException(e); //TODO: Change In Exceptions Section
        }
    }

    private void removeImage(String profileImagesPath, String storedName) {
        storageProvider.delete(
                profileImagesPath,
                storedName
        );
    }

    @Transactional
    public void setDefaultImage(DbUser user) {

        ImageMetadata metadata = imageMetadataRepository.findImageMetadataByOwnerId(user.getUserId())
                .orElse(null);

        if (metadata != null) {
            String storageName = metadata.getStoredName();
            storageProvider.delete(storageProperties.getProfileImages().getPath(), storageName);
        }

        updateUserImageKey(user.getUserId(), storageProperties.getProfileImages().getDefaultImageKey());

        if (metadata != null)
            imageMetadataRepository.deleteById(metadata.getId());
    }

    @Transactional
    public void updateUserImageKey(UUID userId, String imageKey) {

        userRepository.updateImageKeyByUserId(imageKey, userId);
        applicationEventPublisher.publishEvent(
                new UserProfileImageChangedEvent(userId, imageKey)
        );
    }

    private ResponseEntity<StreamingResponseBody> defaultImage() {

        ClassPathResource resource = new ClassPathResource(storageProperties.getProfileImages().getDefaultImagePath());

        if (!resource.exists())
            throw new RuntimeException("Default avatar not found in resources"); // TODO: Change In Exceptions Section

        StreamingResponseBody body = os -> {
            try (InputStream in = resource.getInputStream()) {
                in.transferTo(os);
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(body);
    }

    private String getFileExtension(String mimetype) {
        return mimetype.split("/")[1];
    }

    private String baseImageValidation(MultipartFile file, List<String> mimeTypes) {
        if (file.isEmpty())
            throw new RuntimeException(); // TODO: Change In Exceptions Section

        String mimetype = file.getContentType();
        if (mimetype == null || !mimeTypes.contains(mimetype))
            throw new RuntimeException(); // TODO: Change In Exceptions Section

        String originalName = file.getOriginalFilename();
        if (originalName == null || cloudStorageServiceUtils.containsPathSeparator(originalName))
            throw new RuntimeException(); // TODO: Change In Exceptions Section

        return mimetype;
    }
}
