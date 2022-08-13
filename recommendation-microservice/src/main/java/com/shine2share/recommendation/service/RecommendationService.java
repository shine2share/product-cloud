package com.shine2share.recommendation.service;

import com.mongodb.DuplicateKeyException;
import com.shine2share.core.exception.InvalidInputException;
import com.shine2share.core.recommendation.Recommendation;
import com.shine2share.recommendation.persistence.RecommendationEntity;
import com.shine2share.recommendation.persistence.RecommendationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

import static java.util.logging.Level.FINE;

@Service
public class RecommendationService {
    private static final Logger LOG = LoggerFactory.getLogger(RecommendationService.class);
    private final RecommendationRepository recommendationRepository;
    public RecommendationService(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    public Flux<Recommendation> getRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        LOG.info("Will get recommendations for product with id={}", productId);
        return recommendationRepository.findByProductId(productId)
                .log(LOG.getName(), FINE)
                .map(this::entityToApi);
    }

    public Mono<Recommendation> createRecommendations(Recommendation body) {
        if (body.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.getProductId());
        }
        return this.recommendationRepository.save(apiToEntity(body))
                .log(LOG.getName(), Level.FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId()))
                .map(this::entityToApi);
    }
    public Mono<Void> deleteRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        return this.recommendationRepository.deleteAll(this.recommendationRepository.findByProductId(productId));
    }
    private RecommendationEntity apiToEntity(Recommendation body) {
        RecommendationEntity recommendationEntity = new RecommendationEntity();
        recommendationEntity.setRecommendationId(body.getRecommendationId());
        recommendationEntity.setProductId(body.getProductId());
        recommendationEntity.setAuthor(body.getAuthor());
        recommendationEntity.setContent(body.getContent());
        recommendationEntity.setRating(body.getRate());
        return recommendationEntity;
    }
    private Recommendation entityToApi(RecommendationEntity e) {
        return new Recommendation(e.getProductId(),
                e.getRecommendationId(),
                e.getAuthor(),
                e.getRating(),
                e.getContent(),
                "");
    }
}
