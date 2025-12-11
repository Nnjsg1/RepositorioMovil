package com.levelup.backend.controller;

import com.levelup.backend.dto.CartDTO;
import com.levelup.backend.model.Cart;
import com.levelup.backend.model.CartId;
import com.levelup.backend.model.User;
import com.levelup.backend.model.Product;
import com.levelup.backend.repository.CartRepository;
import com.levelup.backend.repository.UserRepository;
import com.levelup.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@Transactional
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    // Obtener carrito de un usuario
    @GetMapping("/{userId}")
    public ResponseEntity<List<CartDTO>> getCartByUser(@PathVariable Integer userId) {
        List<CartDTO> cart = cartRepository.findByUser_Id(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cart);
    }

    // Agregar producto al carrito
    @PostMapping
    public ResponseEntity<CartDTO> addToCart(@RequestBody CartDTO cartDTO) {
        User user = userRepository.findById(cartDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Product product = productRepository.findById(cartDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        CartId cartId = new CartId(cartDTO.getUserId(), cartDTO.getProductId());
        Cart cart = cartRepository.findById(cartId)
                .map(existingCart -> {
                    existingCart.setQuantity(existingCart.getQuantity() + cartDTO.getQuantity());
                    return existingCart;
                })
                .orElseGet(() -> new Cart(user, product, cartDTO.getQuantity()));

        Cart savedCart = cartRepository.save(cart);
        return ResponseEntity.ok(convertToDTO(savedCart));
    }

    // Actualizar cantidad de un producto en el carrito
    @PutMapping("/{userId}/{productId}")
    public ResponseEntity<CartDTO> updateCartItem(@PathVariable Integer userId, 
                                                   @PathVariable Integer productId, 
                                                   @RequestBody CartDTO cartDTO) {
        CartId cartId = new CartId(userId, productId);
        return cartRepository.findById(cartId)
                .map(cart -> {
                    cart.setQuantity(cartDTO.getQuantity());
                    Cart updatedCart = cartRepository.save(cart);
                    return ResponseEntity.ok(convertToDTO(updatedCart));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar producto del carrito
    @DeleteMapping("/{userId}/{productId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Integer userId, @PathVariable Integer productId) {
        CartId cartId = new CartId(userId, productId);
        if (cartRepository.existsById(cartId)) {
            cartRepository.deleteById(cartId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Vaciar carrito de un usuario
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Integer userId) {
        List<Cart> userCart = cartRepository.findByUser_Id(userId);
        cartRepository.deleteAll(userCart);
        return ResponseEntity.noContent().build();
    }

    // Convertir Cart a CartDTO
    private CartDTO convertToDTO(Cart cart) {
        return new CartDTO(
                cart.getUser().getId(),
                cart.getProduct().getId(),
                cart.getProduct().getTitle(),
                cart.getProduct().getImage(),
                cart.getProduct().getPrice(),
                cart.getProduct().getCurrency(),
                cart.getQuantity(),
                cart.getAddedAt(),
                cart.getUpdatedAt()
        );
    }
}
