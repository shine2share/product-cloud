package com.shine2share.composite.controller;

import com.shine2share.composite.service.ProductCompositeService;
import com.shine2share.core.composite.ProductAggregate;
import com.shine2share.core.composite.RecommendationSummary;
import com.shine2share.core.composite.ReviewSummary;
import com.shine2share.core.composite.ServiceAddresses;
import com.shine2share.core.exception.NotFoundException;
import com.shine2share.core.product.Product;
import com.shine2share.core.recommendation.Recommendation;
import com.shine2share.core.review.Review;
import com.shine2share.core.utils.ServiceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product-composite")
@Tag(name = "ProductCompositeController", description =
        "REST API for composite product information.")
public class ProductCompositeController {
    private final ServiceUtil serviceUtil;
    private final ProductCompositeService productCompositeService;
    public ProductCompositeController(
            ServiceUtil serviceUtil, ProductCompositeService productCompositeService) {
        this.serviceUtil = serviceUtil;
        this.productCompositeService = productCompositeService;
    }

    @GetMapping("/{productId}")
    @Operation(
            summary =
                    "${api.product-composite.get-composite-product.description}",
            description =
                    "${api.product-composite.get-composite-product.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description =
                    "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "400", description =
                    "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "404", description =
                    "${api.responseCodes.notFound.description}"),
            @ApiResponse(responseCode = "422", description =
                    "${api.responseCodes.unprocessableEntity.description}")
    })
    public ProductAggregate getProduct(@PathVariable int productId) {
        Product product = productCompositeService.getProduct(productId);
        if (product == null) {
            throw new NotFoundException("No product found for productId: " + productId);
        }
        List<Recommendation> recommendations = productCompositeService.getRecommendations(productId);
        List<Review> reviews = productCompositeService.getReviews(productId);
        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    private ProductAggregate createProductAggregate(
            Product product,
            List<Recommendation> recommendations,
            List<Review> reviews,
            String serviceAddress) {

        // 1. Setup product info
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        // 2. Copy summary recommendation info, if available
        List<RecommendationSummary> recommendationSummaries =
                (recommendations == null) ? null : recommendations.stream()
                        .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate()))
                        .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries =
                (reviews == null) ? null : reviews.stream()
                        .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject()))
                        .collect(Collectors.toList());

        // 4. Create info regarding the involved microservices addresses
        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
