package com.shine2share.product.controller;

import com.shine2share.core.product.Product;
import com.shine2share.core.utils.ServiceUtil;
import com.shine2share.product.service.ProductService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ServiceUtil serviceUtil;
    private final ProductService productService;

    public ProductController(ServiceUtil serviceUtil, ProductService productService) {
        this.serviceUtil = serviceUtil;
        this.productService = productService;
    }

    @GetMapping("/{productId}")
    Product getProduct(@PathVariable int productId) {
        return new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
    }
    @PostMapping("/product")
    Product createProduct(@RequestBody Product body) {
        return productService.createProduct(body);
    }
    @DeleteMapping("/product/{productId}")
    void deleteProduct(@PathVariable int productId) {
        productService.deleteProduct(productId);
    }
}
