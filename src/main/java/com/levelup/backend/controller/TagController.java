package com.levelup.backend.controller;

import com.levelup.backend.dto.TagDTO;
import com.levelup.backend.model.Tag;
import com.levelup.backend.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagRepository tagRepository;

    // Obtener todos los tags
    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        List<TagDTO> tags = tagRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tags);
    }

    // Obtener tag por ID
    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> getTagById(@PathVariable Integer id) {
        return tagRepository.findById(id)
                .map(tag -> ResponseEntity.ok(convertToDTO(tag)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear nuevo tag
    @PostMapping
    public ResponseEntity<TagDTO> createTag(@RequestBody TagDTO tagDTO) {
        Tag tag = convertToEntity(tagDTO);
        Tag savedTag = tagRepository.save(tag);
        return ResponseEntity.ok(convertToDTO(savedTag));
    }

    // Actualizar tag
    @PutMapping("/{id}")
    public ResponseEntity<TagDTO> updateTag(@PathVariable Integer id, @RequestBody TagDTO tagDTO) {
        return tagRepository.findById(id)
                .map(tag -> {
                    tag.setName(tagDTO.getName());
                    Tag updatedTag = tagRepository.save(tag);
                    return ResponseEntity.ok(convertToDTO(updatedTag));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar tag
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Integer id) {
        if (tagRepository.existsById(id)) {
            tagRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Convertir Tag a TagDTO
    private TagDTO convertToDTO(Tag tag) {
        return new TagDTO(tag.getId(), tag.getName());
    }

    // Convertir TagDTO a Tag
    private Tag convertToEntity(TagDTO tagDTO) {
        return new Tag(tagDTO.getName());
    }
}
