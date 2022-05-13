package com.shine2share.review.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReviewRepository extends CrudRepository<ReviewEntity, String> {
    @Transactional(readOnly = true)
    List<ReviewEntity> findByProductId(int productId);
}
