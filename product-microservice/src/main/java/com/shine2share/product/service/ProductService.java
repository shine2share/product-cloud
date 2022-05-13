package com.shine2share.product.service;

import com.shine2share.core.exception.InvalidInputException;
import com.shine2share.core.exception.NotFoundException;
import com.shine2share.core.product.Product;
import com.shine2share.core.utils.ServiceUtil;
import com.shine2share.product.persistence.ProductEntity;
import com.shine2share.product.persistence.ProductRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
@Service
public class ProductService {
    private final ProductRepository repository;
    private final ServiceUtil serviceUtil;
    public ProductService(ProductRepository repository, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.serviceUtil = serviceUtil;
    }

    public Product createProduct(Product body) {
        try {
            ProductEntity entity = new ProductEntity();
            entity.setProductId(body.getProductId());
            entity.setName(body.getName());
            entity.setWeight(body.getWeight());
            ProductEntity newEntity = repository.save(entity);
            Product product = new Product();
            product.setProductId(newEntity.getProductId());
            product.setName(newEntity.getName());
            product.setWeight(newEntity.getWeight());
            return product;
        } catch (DuplicateKeyException e) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId());
        }
    }
    public Product getProduct(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
        ProductEntity entity = repository.findByProductId(productId).orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));
        Product response = new Product();
        response.setWeight(entity.getWeight());
        response.setProductId(entity.getProductId());
        response.setName(entity.getName());
        response.setServiceAddress(serviceUtil.getServiceAddress());
        return response;
    }
    public void deleteProduct(int productId) {
        repository.findByProductId(productId).ifPresent(e -> repository.delete(e));
    }
}
