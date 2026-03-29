package com.example.DoantotnghiepIJ.dto.Menu;



import lombok.Data;

@Data
public class MenuItemDto {

    private String name;
    private String slug;
    private String description;
    private Double price;
    private Double discountPrice;
    private Long categoryId;
    private Integer quantity;
    private Boolean isActive;
}