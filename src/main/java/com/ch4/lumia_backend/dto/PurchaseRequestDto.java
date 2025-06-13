package com.ch4.lumia_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseRequestDto {
    private String itemId;
    private String itemName;
    private int cost;
}