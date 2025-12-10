package com.levelup.backend.controller;

import com.levelup.backend.dto.UserDTO;
import com.levelup.backend.model.User;
import com.levelup.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // Obtener todos los usuarios
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // Obtener usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(convertToDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear nuevo usuario
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        try {
            logger.info("========== CREAR USUARIO - INICIO ==========");
            logger.info("Datos recibidos del frontend:");
            logger.info("  - Name: {}", userDTO.getName());
            logger.info("  - Email: {}", userDTO.getEmail());
            logger.info("  - Clave: {}", userDTO.getClave() != null ? "[PRESENTE]" : "[NULL]");
            logger.info("  - IsAdmin: {}", userDTO.getIsAdmin());
            
            User user = convertToEntity(userDTO);
            logger.info("Usuario convertido a entidad:");
            logger.info("  - Name: {}", user.getName());
            logger.info("  - Email: {}", user.getEmail());
            logger.info("  - IsAdmin: {}", user.getIsAdmin());
            
            User savedUser = userService.createUser(user);
            logger.info("Usuario guardado exitosamente con ID: {}", savedUser.getId());
            logger.info("========== CREAR USUARIO - FIN ==========");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedUser));
        } catch (RuntimeException e) {
            logger.error("Error al crear usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al crear usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear usuario: " + e.getMessage());
        }
    }

    // Actualizar usuario
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody UserDTO userDTO) {
        try {
            User userDetails = new User();
            userDetails.setName(userDTO.getName());
            userDetails.setEmail(userDTO.getEmail());
            userDetails.setClave(userDTO.getClave());
            userDetails.setIsAdmin(userDTO.getIsAdmin());
            
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(convertToDTO(updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Desactivar usuario (soft delete)
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Integer id) {
        try {
            logger.info("Desactivando usuario con ID: {}", id);
            User deactivatedUser = userService.deactivateUser(id);
            return ResponseEntity.ok(convertToDTO(deactivatedUser));
        } catch (RuntimeException e) {
            logger.error("Error al desactivar usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Activar usuario
    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Integer id) {
        try {
            logger.info("Activando usuario con ID: {}", id);
            User activatedUser = userService.activateUser(id);
            return ResponseEntity.ok(convertToDTO(activatedUser));
        } catch (RuntimeException e) {
            logger.error("Error al activar usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Obtener solo usuarios activos
    @GetMapping("/active")
    public ResponseEntity<List<UserDTO>> getActiveUsers() {
        List<UserDTO> users = userService.getActiveUsers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // Obtener solo usuarios inactivos
    @GetMapping("/inactive")
    public ResponseEntity<List<UserDTO>> getInactiveUsers() {
        List<UserDTO> users = userService.getInactiveUsers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // Buscar usuario por email
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(user -> ResponseEntity.ok(convertToDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Convertir User a UserDTO
    private UserDTO convertToDTO(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getEmail(), 
                          user.getClave(), user.getIsAdmin(), user.getActive(), user.getCreatedAt());
    }

    // Convertir UserDTO a User
    private User convertToEntity(UserDTO userDTO) {
        User user = new User(userDTO.getName(), userDTO.getEmail(), userDTO.getClave());
        user.setIsAdmin(userDTO.getIsAdmin() != null ? userDTO.getIsAdmin() : false);
        user.setActive(userDTO.getActive() != null ? userDTO.getActive() : true);
        return user;
    }
}
