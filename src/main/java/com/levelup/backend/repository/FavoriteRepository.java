package com.levelup.backend.repository;

import com.levelup.backend.model.Favorite;
import com.levelup.backend.model.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    List<Favorite> findByUser_Id(Integer userId);
    List<Favorite> findByProduct_Id(Integer productId);
}
