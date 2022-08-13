package com.shine2share.review.service;

import com.shine2share.core.exception.InvalidInputException;
import com.shine2share.core.review.Review;
import com.shine2share.core.utils.ServiceUtil;
import com.shine2share.review.controller.ReviewController;
import com.shine2share.review.persistence.ReviewEntity;
import com.shine2share.review.persistence.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Service
public class ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewController.class);
    private final ServiceUtil serviceUtil;
    private final Scheduler jdbcScheduler;
    private final ReviewRepository repository;
    public ReviewService(ServiceUtil serviceUtil, @Qualifier("jdbcScheduler") Scheduler jdbcScheduler, ReviewRepository repository) {
        this.serviceUtil = serviceUtil;
        this.jdbcScheduler = jdbcScheduler;
        this.repository = repository;
    }
    public Flux<Review> getReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        return Mono.fromCallable(() -> internalGetReviews(productId))
                .flatMapMany(Flux::fromIterable)
                .log("review controller log", Level.FINE)
                .subscribeOn(jdbcScheduler);
    }
    private List<Review> internalGetReviews(int productId) {
        List<ReviewEntity> entities = this.repository.findByProductId(productId);
        List<Review> reviews = new ArrayList<>();
        for (ReviewEntity entity : entities) {
            reviews.add(entityToApi(entity));
        }
        reviews.forEach(e -> e.setServiceAddress(this.serviceUtil.getServiceAddress()));
        return reviews;
    }
    public Mono<Review> createReview(Review body) {
        if (body.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.getProductId());
        }
        return Mono.fromCallable(() -> internalCreateReview(body))
                .subscribeOn(jdbcScheduler);
    }
    private Review internalCreateReview(Review body) {
        try {
            ReviewEntity entity = apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);
            LOG.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
            return entityToApi(newEntity);

        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Review Id:" + body.getReviewId());
        }
    }
    private ReviewEntity apiToEntity(Review body) {
        ReviewEntity entity = new ReviewEntity();
        entity.setProductId(body.getProductId());
        entity.setReviewId(body.getReviewId());
        entity.setAuthor(body.getAuthor());
        entity.setSubject(body.getSubject());
        entity.setContent(body.getContent());
        return entity;
    }
    private Review entityToApi(ReviewEntity entity) {
        return new Review(
                entity.getProductId(),
                entity.getReviewId(),
                entity.getAuthor(),
                entity.getSubject(),
                entity.getContent(),
                ""
        );
    }
    public Mono<Void> deleteReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        return Mono.fromRunnable(() -> internalDeleteReviews(productId)).subscribeOn(jdbcScheduler).then();
    }
    private void internalDeleteReviews(int productId) {
        LOG.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
