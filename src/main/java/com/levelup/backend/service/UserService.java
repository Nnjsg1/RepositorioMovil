package com.levelup.backend.service;

import com.levelup.backend.model.User;
import com.levelup.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User createUser(User user) {
        logger.info("UserService - Creando usuario: {}", user.getEmail());
        
        // Validar que no exista usuario con el mismo email
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            logger.error("Usuario con email {} ya existe", user.getEmail());
            throw new RuntimeException("Ya existe un usuario con el email: " + user.getEmail());
        }
        
        // Asegurar que isAdmin tenga un valor por defecto
        if (user.getIsAdmin() == null) {
            user.setIsAdmin(false);
        }
        
        User savedUser = userRepository.save(user);
        logger.info("Usuario creado exitosamente con ID: {}", savedUser.getId());
        
        return savedUser;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User updateUser(Integer id, User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(userDetails.getName());
                    user.setEmail(userDetails.getEmail());
                    user.setClave(userDetails.getClave());
                    if (userDetails.getIsAdmin() != null) {
                        user.setIsAdmin(userDetails.getIsAdmin());
                    }
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
    }

    @Transactional
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con id: " + id);
        }
        userRepository.deleteById(id);
    }
}
