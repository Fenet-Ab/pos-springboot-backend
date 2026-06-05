package com.pos.app.controller;

import com.pos.app.dto.request.ProductRequest;
import com.pos.app.dto.response.ApiResponse;
import com.pos.app.dto.response.ProductResponse;
import com.pos.app.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ApiResponse<ProductResponse> create(
            @Valid @RequestBody ProductRequest request
    ) {
        return ApiResponse.success(
                "Product created",
                productService.create(request)
        );
    }

    @GetMapping
    public ApiResponse<List<ProductResponse>> getAll() {
        return ApiResponse.success(
                "Products fetched",
                productService.getAll()
        );
    }
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getById(@PathVariable Long id ) {
        return ApiResponse.success(
                "Product found",
                productService.getById(id)
        );
    }
    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        ProductResponse response =
                productService.updateById(id, request);

        return ApiResponse.success(
                "Product updated successfully",
                response
        );
    }
    @DeleteMapping("/{id}")
    public ApiResponse<ProductResponse> delete(@PathVariable Long id) {
        productService.deleteById(id);
        return ApiResponse.success(
                "Product deleted successfully",
                null
        );
    }
}