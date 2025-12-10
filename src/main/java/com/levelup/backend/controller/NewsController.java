package com.levelup.backend.controller;

import com.levelup.backend.dto.NewsDTO;
import com.levelup.backend.model.News;
import com.levelup.backend.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/news")
@Transactional
public class NewsController {

    @Autowired
    private NewsRepository newsRepository;

    // Obtener todas las noticias
    @GetMapping
    public ResponseEntity<List<NewsDTO>> getAllNews() {
        List<NewsDTO> news = newsRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(news);
    }

    // Obtener solo noticias publicadas
    @GetMapping("/published")
    public ResponseEntity<List<NewsDTO>> getPublishedNews() {
        List<NewsDTO> news = newsRepository.findByIsPublishedTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(news);
    }

    // Obtener noticia por ID
    @GetMapping("/{id}")
    public ResponseEntity<NewsDTO> getNewsById(@PathVariable Long id) {
        return newsRepository.findById(id)
                .map(news -> ResponseEntity.ok(convertToDTO(news)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener noticias por categoría
    @GetMapping("/category/{category}")
    public ResponseEntity<List<NewsDTO>> getNewsByCategory(@PathVariable String category) {
        List<NewsDTO> news = newsRepository.findByCategory(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(news);
    }

    // Obtener noticias publicadas por categoría
    @GetMapping("/category/{category}/published")
    public ResponseEntity<List<NewsDTO>> getPublishedNewsByCategory(@PathVariable String category) {
        List<NewsDTO> news = newsRepository.findByCategoryAndIsPublishedTrue(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(news);
    }

    // Buscar noticias por título
    @GetMapping("/search/{title}")
    public ResponseEntity<List<NewsDTO>> searchNews(@PathVariable String title) {
        List<NewsDTO> news = newsRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(news);
    }

    // Crear nueva noticia
    @PostMapping
    public ResponseEntity<NewsDTO> createNews(@RequestBody NewsDTO newsDTO) {
        News news = convertToEntity(newsDTO);
        News savedNews = newsRepository.save(news);
        return ResponseEntity.ok(convertToDTO(savedNews));
    }

    // Actualizar noticia
    @PutMapping("/{id}")
    public ResponseEntity<NewsDTO> updateNews(@PathVariable Long id, @RequestBody NewsDTO newsDTO) {
        return newsRepository.findById(id)
                .map(news -> {
                    news.setTitle(newsDTO.getTitle());
                    news.setContent(newsDTO.getContent());
                    news.setSummary(newsDTO.getSummary());
                    news.setImage(newsDTO.getImage());
                    news.setThumbnail(newsDTO.getThumbnail());
                    news.setAuthor(newsDTO.getAuthor());
                    news.setCategory(newsDTO.getCategory());
                    news.setIsPublished(newsDTO.getIsPublished());
                    news.setViews(newsDTO.getViews());
                    News updatedNews = newsRepository.save(news);
                    return ResponseEntity.ok(convertToDTO(updatedNews));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar noticia
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        if (newsRepository.existsById(id)) {
            newsRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Incrementar vistas
    @PostMapping("/{id}/view")
    public ResponseEntity<NewsDTO> incrementViews(@PathVariable Long id) {
        return newsRepository.findById(id)
                .map(news -> {
                    news.setViews(news.getViews() != null ? news.getViews() + 1 : 1);
                    News updatedNews = newsRepository.save(news);
                    return ResponseEntity.ok(convertToDTO(updatedNews));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Convertir News a NewsDTO
    private NewsDTO convertToDTO(News news) {
        return new NewsDTO(
                news.getId(),
                news.getTitle(),
                news.getContent(),
                news.getSummary(),
                news.getImage(),
                news.getThumbnail(),
                news.getAuthor(),
                news.getCategory(),
                news.getViews(),
                news.getIsPublished(),
                news.getCreatedAt(),
                news.getUpdatedAt()
        );
    }

    // Convertir NewsDTO a News
    private News convertToEntity(NewsDTO newsDTO) {
        News news = new News(
                newsDTO.getTitle(),
                newsDTO.getContent(),
                newsDTO.getSummary(),
                newsDTO.getImage(),
                newsDTO.getAuthor(),
                newsDTO.getCategory()
        );
        news.setThumbnail(newsDTO.getThumbnail());
        if (newsDTO.getViews() != null) {
            news.setViews(newsDTO.getViews());
        }
        if (newsDTO.getIsPublished() != null) {
            news.setIsPublished(newsDTO.getIsPublished());
        }
        return news;
    }
}
