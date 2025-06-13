package com.ch4.lumia_backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EquippedItemsUpdateRequestDto {
    private List<String> equippedItems;
}