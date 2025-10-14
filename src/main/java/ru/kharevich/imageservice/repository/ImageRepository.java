package ru.kharevich.imageservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kharevich.imageservice.model.Image;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {
    Optional<Image> findByUrl(String url);
    List<Image> findByParentEntityId(UUID parentId);
}
