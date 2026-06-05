package com.pos.app.service;

import com.pos.app.dto.request.ProductRequest;
import com.pos.app.dto.response.ProductResponse;
import com.pos.app.model.entity.Product;
import com.pos.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse create(ProductRequest request) {

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .imageUrl(request.getImageUrl())
                .category(request.getCategory())
                .build();

        return map(productRepository.save(product));
    }

    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }


    private ProductResponse map(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .quantity(p.getQuantity())
                .imageUrl(p.getImageUrl())
                .category(p.getCategory().name())
                .build();
    }
    public ProductResponse getById(Long id) {
        return map(productRepository.getOne(id));

    }
    public ProductResponse updateById(Long id, ProductRequest request) {
        Product product = productRepository.getOne(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(request.getCategory());
        return map(productRepository.save(product));
    }
    public void deleteById(Long id) {
        productRepository.deleteById(id);

    }
}