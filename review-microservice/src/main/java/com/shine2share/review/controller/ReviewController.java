package com.shine2share.review.controller;

import com.shine2share.core.exception.InvalidInputException;
import com.shine2share.core.review.Review;
import com.shine2share.core.utils.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/review")
public class ReviewController {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewController.class);
    private final ServiceUtil serviceUtil;
    public ReviewController(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }
    @GetMapping("/{productId}")
    public List<Review> getReviews(@PathVariable int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        if (productId == 213) {
            LOG.debug("No reviews found for productId: {}", productId);
            return new ArrayList<>();
        }

        List<Review> list = new ArrayList<>();
        list.add(new Review(productId, 1, "Author 1", "Subject 1", "Content 1", serviceUtil.getServiceAddress()));
        list.add(new Review(productId, 2, "Author 2", "Subject 2", "Content 2", serviceUtil.getServiceAddress()));
        list.add(new Review(productId, 3, "Author 3", "Subject 3", "Content 3", serviceUtil.getServiceAddress()));

        LOG.debug("/reviews response size: {}", list.size());

        return list;
    }
    @PostMapping("/review")
    Review createReviews(@RequestBody Review body) {
        return null;
    }
    @DeleteMapping("/review")
    void deleteReviews(@RequestParam(value = "productId") int productId) {

    }
}
