package ru.kharevich.imageservice.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
@Table(name = "comments")
public class Comments {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "payload", nullable = false)
    private String payload;

    @Column(name = "poster_id", nullable = false)
    private UUID posterId;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "leaved_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime leavedAt;

}
