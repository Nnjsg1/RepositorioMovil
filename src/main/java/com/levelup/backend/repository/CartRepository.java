package com.levelup.backend.repository;

import com.levelup.backend.model.Cart;
import com.levelup.backend.model.CartId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, CartId> {
    List<Cart> findByUser_Id(Integer userId);
}
