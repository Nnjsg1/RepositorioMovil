package com.levelup.backend.controller;

import com.levelup.backend.dto.ProductImageDTO;
import com.levelup.backend.model.ProductImage;
import com.levelup.backend.model.Product;
import com.levelup.backend.repository.ProductImageRepository;
import com.levelup.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product-images")
public class ProductImageController {

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductRepository productRepository;

    // Obtener todas las imágenes
    @GetMapping
    public ResponseEntity<List<ProductImageDTO>> getAllProductImages() {
        List<ProductImageDTO> images = productImageRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(images);
    }

    // Obtener imagen por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductImageDTO> getProductImageById(@PathVariable Integer id) {
        return productImageRepository.findById(id)
                .map(image -> ResponseEntity.ok(convertToDTO(image)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener imágenes de un producto
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductImageDTO>> getImagesByProduct(@PathVariable Integer productId) {
        List<ProductImageDTO> images = productImageRepository.findByProduct_Id(productId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(images);
    }

    // Crear nueva imagen
    @PostMapping
    public ResponseEntity<ProductImageDTO> createProductImage(@RequestBody ProductImageDTO imageDTO) {
        ProductImage image = convertToEntity(imageDTO);
        ProductImage savedImage = productImageRepository.save(image);
        return ResponseEntity.ok(convertToDTO(savedImage));
    }

    // Actualizar imagen
    @PutMapping("/{id}")
    public ResponseEntity<ProductImageDTO> updateProductImage(@PathVariable Integer id, @RequestBody ProductImageDTO imageDTO) {
        return productImageRepository.findById(id)
                .map(image -> {
                    image.setUrl(imageDTO.getUrl());
                    image.setPosition(imageDTO.getPosition());
                    ProductImage updatedImage = productImageRepository.save(image);
                    return ResponseEntity.ok(convertToDTO(updatedImage));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar imagen
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductImage(@PathVariable Integer id) {
        if (productImageRepository.existsById(id)) {
            productImageRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Convertir ProductImage a ProductImageDTO
    private ProductImageDTO convertToDTO(ProductImage image) {
        return new ProductImageDTO(image.getId(), image.getProduct().getId(), image.getUrl(), image.getPosition());
    }

    // Convertir ProductImageDTO a ProductImage
    private ProductImage convertToEntity(ProductImageDTO imageDTO) {
        ProductImage image = new ProductImage(imageDTO.getUrl(), imageDTO.getPosition());
        if (imageDTO.getProductId() != null) {
            productRepository.findById(imageDTO.getProductId()).ifPresent(image::setProduct);
        }
        return image;
    }
}
