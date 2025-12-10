package com.levelup.backend.controller;

import com.levelup.backend.dto.LoginRequest;
import com.levelup.backend.dto.LoginResponse;
import com.levelup.backend.dto.UserDTO;
import com.levelup.backend.model.User;
import com.levelup.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        // Buscar usuario por email
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);
        
        if (user == null) {
            return ResponseEntity.ok(new LoginResponse(false, "Usuario no encontrado", null));
        }
        
        // Verificar contraseña (comparación directa, sin encriptación)
        if (!user.getClave().equals(loginRequest.getClave())) {
            return ResponseEntity.ok(new LoginResponse(false, "Contraseña incorrecta", null));
        }
        
        // Login exitoso
        UserDTO userDTO = new UserDTO(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getClave(),
            user.getIsAdmin(),
            user.getCreatedAt()
        );
        
        return ResponseEntity.ok(new LoginResponse(true, "Login exitoso", userDTO));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody UserDTO userDTO) {
        // Verificar si el email ya existe
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            return ResponseEntity.ok(new LoginResponse(false, "El email ya está registrado", null));
        }
        
        // Crear nuevo usuario
        User user = new User(userDTO.getName(), userDTO.getEmail(), userDTO.getClave());
        user.setIsAdmin(userDTO.getIsAdmin() != null ? userDTO.getIsAdmin() : false);
        User savedUser = userRepository.save(user);
        
        UserDTO responseDTO = new UserDTO(
            savedUser.getId(),
            savedUser.getName(),
            savedUser.getEmail(),
            savedUser.getClave(),
            savedUser.getIsAdmin(),
            savedUser.getCreatedAt()
        );
        
        return ResponseEntity.ok(new LoginResponse(true, "Usuario registrado exitosamente", responseDTO));
    }
}
