package com.phucle.annotation.user;

import com.phucle.annotation.processing.Builder;

@Builder
public class Product {
    private String name;

    public Product(String name) {
        this.name = name;
    }
}
