package com.levelup.backend.repository;

import com.levelup.backend.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findByCategory(String category);
    List<News> findByIsPublishedTrue();
    List<News> findByTitleContainingIgnoreCase(String title);
    List<News> findByCategoryAndIsPublishedTrue(String category);
}
