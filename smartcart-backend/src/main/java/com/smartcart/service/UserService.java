package com.smartcart.service;

import com.smartcart.dto.AddressDto;
import com.smartcart.dto.UpdateProfileRequest;
import com.smartcart.dto.UserDto;
import com.smartcart.entity.Address;
import com.smartcart.entity.User;
import com.smartcart.exception.ResourceNotFoundException;
import com.smartcart.repository.AddressRepository;
import com.smartcart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Transactional(readOnly = true)
    public UserDto getUserProfile(Long userId) {
        User user = findUserById(userId);
        return mapToUserDto(user);
    }

    @Transactional
    public UserDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUserById(userId);

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());

        user = userRepository.save(user);
        log.info("Profile updated for user: {}", user.getEmail());
        return mapToUserDto(user);
    }

    @Transactional(readOnly = true)
    public List<AddressDto> getAddresses(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::mapToAddressDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressDto addAddress(Long userId, AddressDto dto) {
        User user = findUserById(userId);

        // If this is the first address or marked as default, handle default logic
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            addressRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(existing -> {
                        existing.setIsDefault(false);
                        addressRepository.save(existing);
                    });
        }

        Address address = Address.builder()
                .street(dto.getStreet())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .country(dto.getCountry())
                .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false)
                .user(user)
                .build();

        address = addressRepository.save(address);
        log.info("Address added for user: {}", user.getEmail());
        return mapToAddressDto(address);
    }

    @Transactional
    public AddressDto updateAddress(Long userId, Long addressId, AddressDto dto) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            addressRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(addressId)) {
                            existing.setIsDefault(false);
                            addressRepository.save(existing);
                        }
                    });
        }

        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
        address.setCountry(dto.getCountry());
        if (dto.getIsDefault() != null) address.setIsDefault(dto.getIsDefault());

        address = addressRepository.save(address);
        return mapToAddressDto(address);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        addressRepository.delete(address);
        log.info("Address deleted: {}", addressId);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AddressDto mapToAddressDto(Address address) {
        return AddressDto.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .build();
    }
}
