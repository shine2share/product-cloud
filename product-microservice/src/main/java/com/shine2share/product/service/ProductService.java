package com.shine2share.product.service;

import com.shine2share.core.exception.InvalidInputException;
import com.shine2share.core.product.Product;
import com.shine2share.core.utils.ServiceUtil;
import com.shine2share.product.controller.ProductController;
import com.shine2share.product.persistence.ProductEntity;
import com.shine2share.product.persistence.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static java.util.logging.Level.FINE;

@Service
public class ProductService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);
    private final ServiceUtil serviceUtil;
    private final ProductRepository repository;
    private final ProductMapper mapper;
    public ProductService(ServiceUtil serviceUtil, ProductRepository repository, ProductMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }
    public Mono<Product> createProduct(Product body) {
        if (body.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.getProductId());
        }
        ProductEntity entity = new ProductEntity();
        entity.setProductId(body.getProductId());
        entity.setName(body.getName());
        entity.setWeight(body.getWeight());
        Mono<Product> newEntity = repository.save(entity)
                .log(LOG.getName(), FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId()))
                .map(e -> mapper.entityToApi(e));

        return newEntity;
    }
    public Mono<Void> deleteProduct(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        return repository.findByProductId(productId).log(LOG.getName(), FINE).map(e -> repository.delete(e)).flatMap(e -> e);
    }
}
