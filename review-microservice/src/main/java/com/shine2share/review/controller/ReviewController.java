package com.shine2share.review.controller;

import com.shine2share.core.review.Review;
import com.shine2share.review.service.ReviewService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/review")
public class ReviewController {
    private final ReviewService service;
    public ReviewController(ReviewService service) {
        this.service = service;
    }
    @GetMapping("/{productId}")
    public Flux<Review> getReviews(@PathVariable int productId) {
        return this.service.getReviews(productId);
    }
    @PostMapping("/review")
    public Mono<Review> createReviews(@RequestBody Review body) {
        return this.service.createReview(body);
    }
    @DeleteMapping("/review")
    public Mono<Void> deleteReviews(@RequestParam(value = "productId") int productId) {
        return this.service.deleteReviews(productId);
    }
}
