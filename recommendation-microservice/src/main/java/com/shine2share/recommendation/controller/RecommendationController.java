package com.shine2share.recommendation.controller;

import com.shine2share.core.exception.InvalidInputException;
import com.shine2share.core.recommendation.Recommendation;
import com.shine2share.core.utils.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/recommendation")
public class RecommendationController {
    private static final Logger LOG = LoggerFactory.getLogger(RecommendationController.class);
    private final ServiceUtil serviceUtil;
    public RecommendationController(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }
    @GetMapping("/{productId}")
    public List<Recommendation> getRecommendations(@PathVariable int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        if (productId == 113) {
            LOG.debug("No recommendations found for productId: {}", productId);
            return new ArrayList<>();
        }
        List<Recommendation> list = new ArrayList<>();
        list.add(new Recommendation(productId, 1, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()));
        list.add(new Recommendation(productId, 2, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()));
        list.add(new Recommendation(productId, 3, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress()));
        LOG.debug("/recommendation response size: {}", list.size());
        return list;
    }

    @PostMapping("/recommendation")
    Recommendation createRecommendations(@RequestBody Recommendation body) {
        return null;
    }
    @DeleteMapping("/recommendation")
    void deleteRecommendations(@RequestParam(value = "productId") int productId) {

    }
}
