package com.dk.userservice.service;

import com.dk.userservice.dtos.UserRequestDTO;
import com.dk.userservice.dtos.UserResponseDTO;
import com.dk.userservice.exception.ResourceNotFoundException;
import com.dk.userservice.kafkaservice.UserProducer;
import com.dk.userservice.mapper.UserMapper;
import com.dk.userservice.models.User;
import com.dk.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private  final UserProducer userProducer;
    private final EmailOutBoxService outBoxService;

    @Transactional
    public UserResponseDTO registerUser(UserRequestDTO dto) {
        try {
            User user = UserMapper.toEntity(dto);
            user.setRoles(List.of("USER"));
            user.setPassword(passwordEncoder.encode(dto.getPassword()));

            User user1 = userRepository.save(user);
            outBoxService.createOutBox(user,"CREATE_USER");
           // userProducer.sendUserCreatedEvent(user1.toString());
           // log.info("data sent to kafka broker ");
            return UserMapper.toResponse(user1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register user", e);
        }
    }

    public List<UserResponseDTO> getAllUsers() {
        try {
            return userRepository.findAll().stream()
                    .map(UserMapper::toResponse)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch users", e);
        }
    }

    public UserResponseDTO getUserById(String id) {
        try {
            return userRepository.findById(id)
                    .map(UserMapper::toResponse)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving user by ID", e);
        }
    }

    public UserResponseDTO getUserByUsername(String username) {
        try {
            return userRepository.findByUserName(username)
                    .map(UserMapper::toResponse)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving user by username", e);
        }
    }

    public UserResponseDTO updateUser(String id, UserRequestDTO dto) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            user.setUserName(dto.getUserName());
            user.setEmail(dto.getEmail());
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setPhone(dto.getPhone());
            user.setDateOfBirth(dto.getDateOfBirth());
            user.setGender(User.Gender.valueOf(dto.getGender().toUpperCase()));
            user.setActive(dto.getActive());
            return UserMapper.toResponse(userRepository.save(user));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    public void deleteUser(String id) {
        try {
            userRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }
}