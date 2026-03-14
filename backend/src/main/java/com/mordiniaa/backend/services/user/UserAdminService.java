package com.mordiniaa.backend.services.user;

import com.mordiniaa.backend.config.StorageProperties;
import com.mordiniaa.backend.dto.user.UserDto;
import com.mordiniaa.backend.events.user.events.UserCreatedEvent;
import com.mordiniaa.backend.events.user.events.UserDeleteEvent;
import com.mordiniaa.backend.events.user.events.UserUsernameChangedEvent;
import com.mordiniaa.backend.exceptions.*;
import com.mordiniaa.backend.mappers.user.UserMapper;
import com.mordiniaa.backend.models.user.mysql.*;
import com.mordiniaa.backend.repositories.mysql.AddressRepository;
import com.mordiniaa.backend.repositories.mysql.ContactRepository;
import com.mordiniaa.backend.repositories.mysql.RoleRepository;
import com.mordiniaa.backend.repositories.mysql.UserRepository;
import com.mordiniaa.backend.request.user.AddressRequest;
import com.mordiniaa.backend.request.user.ContactDataRequest;
import com.mordiniaa.backend.request.user.CreateUserRequest;
import com.mordiniaa.backend.request.user.PasswordRequest;
import com.mordiniaa.backend.request.user.patch.PatchUserAddressRequest;
import com.mordiniaa.backend.request.user.patch.PatchUserContactDataRequest;
import com.mordiniaa.backend.request.user.patch.PatchUserDataRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StorageProperties storageProperties;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MongoUserService mongoUserService;
    private final UserService userService;
    private final AddressRepository addressRepository;
    private final ContactRepository contactRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto createUser(CreateUserRequest request) {

        String firstName = request.getFirstname().trim();
        String lastName = request.getLastname().trim();

        if (userRepository.existsUserByFirstNameAndLastName(firstName, lastName))
            throw new BadRequestException("User Already Exists");

        String login = request.getUsername() != null
                ? request.getUsername()
                : generateUniqueLogin(firstName, lastName);

        Role userRole = roleRepository.findRoleByAppRole(request.getRole())
                .orElseGet(() -> roleRepository.findRoleByAppRole(AppRole.ROLE_USER)
                        .orElseThrow(RoleNotFoundException::new)
                );

        User newUser = new User();
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setRole(userRole);
        newUser.setUsername(login);
        newUser.setImageKey(storageProperties.getProfileImages().getDefaultImageKey());

        //Address
        var addr = request.getAddress();
        if (addr != null) {
            Address address = new Address();
            setupFullAddress(address, addr);
            address.setUser(newUser);
            newUser.addAddress(address);
        }

        //Contact Data
        var contactData = request.getContactData();
        if (contactData != null) {
            Contact contact = new Contact();
            setupFullContactData(contact, contactData);
            contact.setUser(newUser);
            newUser.setContact(contact);
        }

        User savedUser = userRepository.save(newUser);
        applicationEventPublisher.publishEvent(
                new UserCreatedEvent(savedUser.getUserId())
        );

        return userMapper.toDto(savedUser);
    }

    @Transactional
    public void updateUserBasicData(UUID userId, PatchUserDataRequest request) {

        mongoUserService.checkUserAvailability(userId);

        String firstName = request.getFirstname();
        String lastName = request.getLastname();

        if (firstName == null && lastName == null)
            throw new BadCredentialsException("This User Already Exists");

        User user = userService.getUser(userId);

        String newFirst = firstName != null ? firstName : user.getFirstName();
        String newLast = lastName != null ? lastName : user.getLastName();

        String username = generateUniqueLogin(newFirst, newLast);

        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);

        user.setUsername(username);
        userRepository.save(user);

        applicationEventPublisher.publishEvent(
                new UserUsernameChangedEvent(userId, username)
        );
    }

    @Transactional
    public void updateUserAddressData(UUID userId, Long addressId, PatchUserAddressRequest request) {

        mongoUserService.checkUserAvailability(userId);

        User user = userService.getUser(userId);
        Address address;

        if (addressId == null) {
            address = new Address();
            setupFullAddress(address, request);
            address.setUser(user);
            addressRepository.save(address);
            return;
        }

        address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(AddressNotFoundException::new);

        if (request.getCountry() != null) address.setCountry(request.getCountry());
        if (request.getCity() != null) address.setCity(request.getCity());
        if (request.getZipCode() != null) address.setZipCode(request.getZipCode());
        if (request.getStreet() != null) address.setStreet(request.getStreet());
        if (request.getDistrict() != null) address.setDistrict(request.getDistrict());

        addressRepository.save(address);
    }

    @Transactional
    public void updateUserContactData(UUID userId, Long contactDataId, PatchUserContactDataRequest request) {

        mongoUserService.checkUserAvailability(userId);

        User user = userService.getUser(userId);
        Contact contact;

        if (contactDataId == null) {
            contact = new Contact();
            setupFullContactData(contact, request);
            contact.setUser(user);
            contactRepository.save(contact);
            return;
        }

        contact = contactRepository.findByIdAndUser(contactDataId, user)
                .orElseThrow(ContactNotFoundException::new);

        if (request.getCountryCallingCode() != null) contact.setCountryCallingCode(request.getCountryCallingCode());
        if (request.getEmail() != null) contact.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) contact.setPhoneNumber(request.getPhoneNumber());

        contactRepository.save(contact);
    }

    @Transactional
    public void deactivateUser(UUID userId) {

        try {
            mongoUserService.checkUserAvailability(userId);
        } catch (UsersNotAvailableException e) {
            throw new UsersNotAvailableException("User Already Not Available");
        }

        userRepository.updateDeletedByUserId(true, userId);
        applicationEventPublisher.publishEvent(
                new UserDeleteEvent(userId)
        );
    }

    private String generateUniqueLogin(String firstName, String lastName) {

        String raw = new Random().nextBoolean()
                ? firstName.substring(0, 3).concat(lastName.substring(0, 3))
                : lastName.substring(0, 3).concat(firstName.substring(0, 3));

        String fullLogin = raw + ThreadLocalRandom.current().nextInt(999);
        while (userRepository.existsByUsername(fullLogin)) {
            fullLogin = raw + ThreadLocalRandom.current().nextInt(999);
        }
        return fullLogin;
    }

    private void setupFullAddress(Address address, AddressRequest r) {

        List<String> missingFields = new ArrayList<>();
        if (r.getStreet() == null) missingFields.add("Street is missing.");
        if (r.getCity() == null) missingFields.add("City is missing.");
        if (r.getCountry() == null) missingFields.add("Country is missing.");
        if (r.getZipCode() == null) missingFields.add("Zipcode is missing.");
        if (r.getDistrict() == null) missingFields.add("District is missing.");

        if (!missingFields.isEmpty()) {
            String message = String.join(" ", missingFields);
            throw new AddressValidationException(message);
        }

        address.setStreet(r.getStreet().trim());
        address.setCountry(r.getCountry().trim());
        address.setCity(r.getCity().trim());
        address.setZipCode(r.getZipCode().trim());
        address.setDistrict(r.getDistrict().trim());
    }

    private void setupFullContactData(Contact contact, ContactDataRequest r) {

        List<String> missingProperties = new ArrayList<>();
        if (r.getCountryCallingCode() == null) missingProperties.add("Calling Code Is Missing.");
        if (r.getEmail() == null) missingProperties.add("Email Is Missing.");
        if (r.getPhoneNumber() == null) missingProperties.add("Phone Number Is Missing.");

        if (!missingProperties.isEmpty()) {
            String message = String.join(" ", missingProperties);
            throw new ContactDataValidationException(message);
        }

        contact.setEmail(r.getEmail().trim());
        contact.setCountryCallingCode(r.getCountryCallingCode().trim());
        contact.setPhoneNumber(r.getPhoneNumber().trim());
    }

    @Transactional
    public void setUserPassword(UUID userId, PasswordRequest passwordRequest) {

        User user = userRepository.findUserByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new BadRequestException("User Not Found"));

        String password = passwordRequest.getPassword();
        String repeatedPassword = passwordRequest.getRepeatedPassword();

        if (!password.equals(repeatedPassword))
            throw new BadRequestException("Password Missmatch");

        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }
}
