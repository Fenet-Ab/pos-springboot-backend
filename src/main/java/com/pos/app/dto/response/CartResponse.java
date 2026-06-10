package com.pos.app.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    private Long cartId;
    private Integer itemCount;
    private BigDecimal totalAmount;
    private List<CartItemResponse> items;
}
