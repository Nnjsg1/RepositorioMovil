package com.levelup.backend.controller;

import com.levelup.backend.dto.FavoriteDTO;
import com.levelup.backend.model.Favorite;
import com.levelup.backend.model.FavoriteId;
import com.levelup.backend.repository.FavoriteRepository;
import com.levelup.backend.repository.UserRepository;
import com.levelup.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    // Obtener todos los favoritos
    @GetMapping
    public ResponseEntity<List<FavoriteDTO>> getAllFavorites() {
        List<FavoriteDTO> favorites = favoriteRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(favorites);
    }

    // Obtener favoritos de un usuario
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FavoriteDTO>> getFavoritesByUser(@PathVariable Integer userId) {
        List<FavoriteDTO> favorites = favoriteRepository.findByUser_Id(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(favorites);
    }

    // Obtener favoritos de un producto
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<FavoriteDTO>> getFavoritesByProduct(@PathVariable Integer productId) {
        List<FavoriteDTO> favorites = favoriteRepository.findByProduct_Id(productId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(favorites);
    }

    // Crear favorito
    @PostMapping
    public ResponseEntity<FavoriteDTO> createFavorite(@RequestBody FavoriteDTO favoriteDTO) {
        Favorite favorite = convertToEntity(favoriteDTO);
        Favorite savedFavorite = favoriteRepository.save(favorite);
        return ResponseEntity.ok(convertToDTO(savedFavorite));
    }

    // Eliminar favorito
    @DeleteMapping("/{userId}/{productId}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable Integer userId, @PathVariable Integer productId) {
        FavoriteId id = new FavoriteId(userId, productId);
        if (favoriteRepository.existsById(id)) {
            favoriteRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Convertir Favorite a FavoriteDTO
    private FavoriteDTO convertToDTO(Favorite favorite) {
        return new FavoriteDTO(favorite.getUser().getId(), favorite.getProduct().getId(), favorite.getAddedAt());
    }

    // Convertir FavoriteDTO a Favorite
    private Favorite convertToEntity(FavoriteDTO favoriteDTO) {
        Favorite favorite = new Favorite();
        userRepository.findById(favoriteDTO.getUserId()).ifPresent(favorite::setUser);
        productRepository.findById(favoriteDTO.getProductId()).ifPresent(favorite::setProduct);
        if (favorite.getUser() != null && favorite.getProduct() != null) {
            favorite.setId(new FavoriteId(favorite.getUser().getId(), favorite.getProduct().getId()));
        }
        return favorite;
    }
}
