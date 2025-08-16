package com.ecommers.orderService.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    private String customerName;
    private String customerEmail;

    private List<OrderLineItemRequest> orderLineItems = new ArrayList<>();

    // Getters and Setters
}