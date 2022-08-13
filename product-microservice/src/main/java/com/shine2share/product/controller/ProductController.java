package com.shine2share.product.controller;

import com.shine2share.core.product.Product;
import com.shine2share.core.utils.ServiceUtil;
import com.shine2share.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/product")
public class ProductController {
    private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);
    private final ServiceUtil serviceUtil;
    private final ProductService productService;
    public ProductController(ServiceUtil serviceUtil, ProductService productService) {
        this.serviceUtil = serviceUtil;
        this.productService = productService;
    }

    @GetMapping("/{productId}")
    Mono<Product> getProduct(@PathVariable int productId) {
        Product result = new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
        return Mono.just(result);
    }
    @PostMapping("/product")
    public Mono<Product> createProduct(@RequestBody Product body) {
        return this.productService.createProduct(body);
    }
    @DeleteMapping("/product/{productId}")
    public Mono<Void> deleteProduct(@PathVariable int productId) {
        return this.productService.deleteProduct(productId);
    }
}
