package com.muralia.repository;

import com.muralia.entity.ImageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, UUID> {

    @Query("SELECT i FROM ImageEntity i ORDER BY i.uploadedAt DESC")
    Page<ImageEntity> findLatestImages(Pageable pageable);

    boolean existsByIdAndCustomerId(UUID imageId, Long customerId);
}
