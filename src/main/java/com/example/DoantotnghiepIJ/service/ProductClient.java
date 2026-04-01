package com.example.DoantotnghiepIJ.service;



import com.example.DoantotnghiepIJ.dto.Menu.MenuItemDto;
import com.example.DoantotnghiepIJ.dto.Menu.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductClient {

    private final RestTemplate restTemplate;

    private final String PRODUCT_API = "http://localhost:8080/api/v1/admin/menu-items/batch";

    public List<ProductResponse> getProducts(List<UUID> ids) {

        List<MenuItemDto> menuItems = restTemplate.exchange(
                PRODUCT_API,
                HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(ids),
                new ParameterizedTypeReference<List<MenuItemDto>>() {}
        ).getBody();

        return menuItems.stream().map(item -> {

            ProductResponse p = new ProductResponse();
            p.setId(item.getId());
            p.setName(item.getName());
            p.setThumbnail(item.getThumbnail());
            p.setPrice(item.getPrice());
            p.setDiscountPrice(item.getDiscountPrice());

            return p;

        }).toList();
    }
}