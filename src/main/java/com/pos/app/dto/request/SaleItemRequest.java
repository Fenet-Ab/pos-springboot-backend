package com.pos.app.dto.request;

import lombok.Data;

@Data
public class SaleItemRequest {

    private Long productId;

    private Integer quantity;
}