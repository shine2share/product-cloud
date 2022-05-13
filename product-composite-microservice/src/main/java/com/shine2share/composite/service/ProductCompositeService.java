package com.shine2share.composite.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shine2share.core.exception.InvalidInputException;
import com.shine2share.core.exception.NotFoundException;
import com.shine2share.core.product.Product;
import com.shine2share.core.recommendation.Recommendation;
import com.shine2share.core.review.Review;
import com.shine2share.core.utils.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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
    private final RestTemplate restTemplate;
    private final String recommendationServiceUrl;

    public ProductCompositeService(
            RestTemplate restTemplate,
            ObjectMapper mapper,
            @Value("${app.product-microservice.host}") String productServiceHost,
            @Value("${app.product-microservice.port}") int productServicePort,
            @Value("${app.recommendation-microservice.host}") String recommendationServiceHost,
            @Value("${app.recommendation-microservice.port}") int recommendationServicePort,
            @Value("${app.review-microservice.host}") String reviewServiceHost,
            @Value("${app.review-microservice.port}") int reviewServicePort
    ) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product/";
        this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation/";
        this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review/";
    }

    public Product getProduct(int productId) {
        try {
            String url = productServiceUrl + productId;
            LOG.debug("Will call getProduct API on URL: {}", url);
            Product product = restTemplate.getForObject(url, Product.class);
            if (product != null)
                LOG.debug("Found a product with id: {}", product.getProductId());
            return product;

        } catch (HttpClientErrorException ex) {
            switch (ex.getStatusCode()) {
                case NOT_FOUND:
                    throw new NotFoundException(getErrorMessage(ex));
                case UNPROCESSABLE_ENTITY:
                    throw new InvalidInputException(getErrorMessage(ex));
                default:
                    LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                    LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                    throw ex;
            }
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException e) {
            return ex.getMessage();
        }
    }

    public List<Recommendation> getRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + productId;
            LOG.debug("Will call getRecommendations API on URL: {}", url);
            List<Recommendation> recommendations = restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {})
                    .getBody();
            if (recommendations != null)
                LOG.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productId);
            return recommendations;
        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Review> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + productId;
            LOG.debug("Will call getReviews API on URL: {}", url);
            List<Review> reviews = restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {})
                    .getBody();
            if (reviews != null)
                LOG.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);
            return reviews;
        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }
}
