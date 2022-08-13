package com.shine2share.product.config;

import com.shine2share.core.event.Event;
import com.shine2share.core.exception.EventProcessingException;
import com.shine2share.core.product.Product;
import com.shine2share.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;


@Configuration
public class MessageProcessorConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);
    private final ProductService productService;
    public MessageProcessorConfig(ProductService productService) {
        this.productService = productService;
    }
    @Bean
    public Consumer<Event<Integer, Product>> messageProcessor() {
        return event -> {
            LOG.info("Pprocess message created at {}...", event.getEventCreatedAt());
            switch (event.getEventType()) {
                case CREATE:
                    Product product = event.getData();
                    LOG.info("Create product with ID: {}", product.getProductId());
                    this.productService.createProduct(product).block();
                    break;
                case DELETE:
                    int productId = event.getKey();
                    LOG.info("Delete recommendations with ProductID: {}", productId);
                    this.productService.deleteProduct(productId).block();
                    break;
                default:
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                    LOG.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
            }
            LOG.info("Message processing done!");
        };
    }
}
