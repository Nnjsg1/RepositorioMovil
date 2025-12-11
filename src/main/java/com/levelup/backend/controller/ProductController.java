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
                    if (productDTO.getDiscontinued() != null) {
                        product.setDiscontinued(productDTO.getDiscontinued());
                    }
                    if (productDTO.getCategoryId() != null) {
                        categoryRepository.findById(productDTO.getCategoryId()).ifPresent(product::setCategory);
                    }
                    Product updatedProduct = productRepository.save(product);
                    return ResponseEntity.ok(convertToDTO(updatedProduct));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Descontinuar producto
    @PatchMapping("/{id}/discontinue")
    public ResponseEntity<ProductDTO> discontinueProduct(@PathVariable Integer id) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setDiscontinued(true);
                    Product updatedProduct = productRepository.save(product);
                    return ResponseEntity.ok(convertToDTO(updatedProduct));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Reactivar producto descontinuado
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<ProductDTO> reactivateProduct(@PathVariable Integer id) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setDiscontinued(false);
                    Product updatedProduct = productRepository.save(product);
                    return ResponseEntity.ok(convertToDTO(updatedProduct));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener productos activos (no descontinuados)
    @GetMapping("/active")
    public ResponseEntity<List<ProductDTO>> getActiveProducts() {
        List<ProductDTO> products = productRepository.findByDiscontinued(false).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    // Obtener productos descontinuados
    @GetMapping("/discontinued")
    public ResponseEntity<List<ProductDTO>> getDiscontinuedProducts() {
        List<ProductDTO> products = productRepository.findByDiscontinued(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
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
                product.getDiscontinued(),
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
        product.setDiscontinued(productDTO.getDiscontinued() != null ? productDTO.getDiscontinued() : false);
        if (productDTO.getCategoryId() != null) {
            categoryRepository.findById(productDTO.getCategoryId()).ifPresent(product::setCategory);
        }
        return product;
    }
}
