package com.shine2share.composite.service;
import static java.util.logging.Level.FINE;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shine2share.core.exception.InvalidInputException;
import com.shine2share.core.exception.NotFoundException;
import com.shine2share.core.product.Product;
import com.shine2share.core.recommendation.Recommendation;
import com.shine2share.core.review.Review;
import com.shine2share.core.utils.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static reactor.core.publisher.Flux.empty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

@Service
public class ProductCompositeService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeService.class);
    private final ObjectMapper mapper;
    private final String reviewServiceUrl;
    private final String productServiceUrl;
    private final WebClient webClient;
    private final String recommendationServiceUrl;

    public ProductCompositeService(
            WebClient.Builder webClient,
            ObjectMapper mapper,
            @Value("${app.product-microservice.host}") String productServiceHost,
            @Value("${app.product-microservice.port}") int productServicePort,
            @Value("${app.recommendation-microservice.host}") String recommendationServiceHost,
            @Value("${app.recommendation-microservice.port}") int recommendationServicePort,
            @Value("${app.review-microservice.host}") String reviewServiceHost,
            @Value("${app.review-microservice.port}") int reviewServicePort
    ) {
        this.webClient = webClient.build();
        this.mapper = mapper;
        this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product/";
        this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation/";
        this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review/";
    }

    public Mono<Product> getProduct(int productId) {
            String url = productServiceUrl + productId;
            LOG.debug("Will call getProduct API on URL: {}", url);
            return webClient.get().uri(url).retrieve()
                    .bodyToMono(Product.class)
                    .log(LOG.getName(), FINE)
                    .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    public Flux<Recommendation> getRecommendations(int productId) {
        String url = recommendationServiceUrl + productId;
        LOG.debug("Will call getRecommendations API on URL: {}", url);
        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return webClient.get().uri(url).retrieve()
                .bodyToFlux(Recommendation.class)
                .log(LOG.getName(), FINE)
                .onErrorResume(error -> empty());
    }

    public Flux<Review> getReviews(int productId) {
        String url = reviewServiceUrl + productId;
        LOG.debug("Will call getReviews API on URL: {}", url);
        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return webClient.get().uri(url).retrieve().bodyToFlux(Review.class).log(LOG.getName(), FINE).onErrorResume(error -> empty());
    }
    private Throwable handleException(Throwable ex) {
        if (!(ex instanceof WebClientResponseException)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }
        WebClientResponseException wcre = (WebClientResponseException)ex;
        switch (wcre.getStatusCode()) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));
            case UNPROCESSABLE_ENTITY :
                return new InvalidInputException(getErrorMessage(wcre));
            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }
    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }
}
