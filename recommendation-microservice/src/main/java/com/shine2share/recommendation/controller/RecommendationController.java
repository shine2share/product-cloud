package com.shine2share.recommendation.controller;

import com.shine2share.core.recommendation.Recommendation;
import com.shine2share.recommendation.service.RecommendationService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/recommendation")
public class RecommendationController {
    private final RecommendationService recommendationService;
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }
    @GetMapping("/{productId}")
    public Flux<Recommendation> getRecommendations(@PathVariable int productId) {
        return this.recommendationService.getRecommendations(productId);
    }

    @PostMapping("/recommendation")
    public Mono<Recommendation> createRecommendations(@RequestBody Recommendation body) {
        return this.recommendationService.createRecommendations(body);
    }
    @DeleteMapping("/recommendation")
    public Mono<Void> deleteRecommendations(@RequestParam(value = "productId") int productId) {
        return this.recommendationService.deleteRecommendations(productId);
    }
}
