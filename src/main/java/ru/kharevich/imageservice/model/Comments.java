package ru.kharevich.imageservice.model;


import jakarta.persistence.*;
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

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "poster_id")
    private User poster;

    @Column(name = "postId", nullable = false)
    private UUID postId;

    @Column(name = "leaved_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime leavedAt;

}
