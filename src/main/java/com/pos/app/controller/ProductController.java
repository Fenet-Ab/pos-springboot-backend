package com.pos.app.controller;

import com.pos.app.dto.request.ProductRequest;
import com.pos.app.dto.response.ApiResponse;
import com.pos.app.dto.response.ProductResponse;
import com.pos.app.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog management")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create product", description = "Requires SUPER_ADMIN, ADMIN, or MANAGER role")
    public ApiResponse<ProductResponse> create(
            @Valid @RequestBody ProductRequest request
    ) {
        return ApiResponse.success(
                "Product created",
                productService.create(request)
        );
    }

    @GetMapping
    @Operation(summary = "List all products")
    public ApiResponse<List<ProductResponse>> getAll() {
        return ApiResponse.success(
                "Products fetched",
                productService.getAll()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ApiResponse<ProductResponse> getById(@PathVariable Long id ) {
        return ApiResponse.success(
                "Product found",
                productService.getById(id)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product")
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
    @Operation(summary = "Delete product")
    public ApiResponse<ProductResponse> delete(@PathVariable Long id) {
        productService.deleteById(id);
        return ApiResponse.success(
                "Product deleted successfully",
                null
        );
    }
}
