package ru.kharevich.imageservice.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;


import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "uploadedAt", nullable = false)
    @CreationTimestamp
    private LocalDateTime uploadedAt;

    @Column(name = "parent_entity_id", nullable = false)
    private UUID parentEntityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false)
    private ImageType imageType;

}
