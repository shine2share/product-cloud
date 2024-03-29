package com.shine2share.recommendation.config;

import com.shine2share.core.event.Event;
import com.shine2share.core.exception.EventProcessingException;
import com.shine2share.core.recommendation.Recommendation;
import com.shine2share.recommendation.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class MessageProcessorConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);
    private final RecommendationService recommendationService;
    public MessageProcessorConfig(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }
    @Bean
    public Consumer<Event<Integer, Recommendation>> messageProcessor() {
        return event -> {
            LOG.info("Process message created at {}...", event.getEventCreatedAt());
            switch (event.getEventType()) {
                case CREATE:
                    Recommendation recommendation = event.getData();
                    LOG.info("Create recommendation with ID: {}/{}", recommendation.getProductId(), recommendation.getRecommendationId());
                    this.recommendationService.createRecommendations(recommendation);
                    break;
                case DELETE:
                    int productId = event.getKey();
                    LOG.info("Delete recommendations with ProductID: {}", productId);
                    this.recommendationService.deleteRecommendations(productId);
                    break;
                default:
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                    LOG.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
            }
        };
    }
}
