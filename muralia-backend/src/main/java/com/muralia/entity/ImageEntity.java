package com.muralia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "images", indexes = {
    @Index(name = "idx_uploaded_at", columnList = "uploaded_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String url;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(length = 255)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column
    private Integer width;

    @Column
    private Integer height;

    @Column(name = "image_data", nullable = false, columnDefinition = "bytea")
    private byte[] imageData;

    @Column(name = "thumbnail_data", columnDefinition = "bytea")
    private byte[] thumbnailData;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_image_customer"))
    private CustomerEntity customer;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private OffsetDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = OffsetDateTime.now();
    }
}
