package com.levelup.backend.controller;

import com.levelup.backend.dto.ProductDTO;
import com.levelup.backend.dto.TagDTO;
import com.levelup.backend.model.Product;
import com.levelup.backend.repository.ProductRepository;
import com.levelup.backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@Transactional
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Obtener todos los productos
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    // Obtener producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer id) {
        return productRepository.findById(id)
                .map(product -> ResponseEntity.ok(convertToDTO(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener productos por categoría
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Integer categoryId) {
        List<ProductDTO> products = productRepository.findByCategory_Id(categoryId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    // Buscar productos por título
    @GetMapping("/search/{title}")
    public ResponseEntity<List<ProductDTO>> searchProducts(@PathVariable String title) {
        List<ProductDTO> products = productRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    // Crear nuevo producto
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        Product product = convertToEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(convertToDTO(savedProduct));
    }

    // Actualizar producto
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Integer id, @RequestBody ProductDTO productDTO) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setTitle(productDTO.getTitle());
                    product.setDescription(productDTO.getDescription());
                    product.setPrice(productDTO.getPrice());
                    product.setCurrency(productDTO.getCurrency());
                    product.setStock(productDTO.getStock());
                    product.setImage(productDTO.getImage());
                    if (productDTO.getCategoryId() != null) {
                        categoryRepository.findById(productDTO.getCategoryId()).ifPresent(product::setCategory);
                    }
                    Product updatedProduct = productRepository.save(product);
                    return ResponseEntity.ok(convertToDTO(updatedProduct));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Convertir Product a ProductDTO
    private ProductDTO convertToDTO(Product product) {
        List<TagDTO> tags = product.getTags() != null ?
                product.getTags().stream()
                        .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                        .collect(Collectors.toList()) : List.of();

        return new ProductDTO(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getStock(),
                product.getImage(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                tags
        );
    }

    // Convertir ProductDTO a Product
    private Product convertToEntity(ProductDTO productDTO) {
        Product product = new Product(
                productDTO.getTitle(),
                productDTO.getDescription(),
                productDTO.getPrice(),
                productDTO.getCurrency()
        );
        product.setStock(productDTO.getStock());
        product.setImage(productDTO.getImage());
        if (productDTO.getCategoryId() != null) {
            categoryRepository.findById(productDTO.getCategoryId()).ifPresent(product::setCategory);
        }
        return product;
    }
}
