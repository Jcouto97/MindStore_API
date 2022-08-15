package com.example.persistence.model.product;

import MindStore.command.productDto.ProductDto;
import MindStore.persistence.models.Person.User;
import MindStore.persistence.models.Product.AverageRating;
import MindStore.persistence.models.Product.Category;
import MindStore.persistence.models.Product.Product;

import javax.persistence.*;
import java.util.Set;

public class ProductPojo {
    public static final Product PRODUCT_EXAMPLE = Product.builder()
            .id(1L)
            .title("bag")
            .price(22.5)
            .description("description")
            .image("imageurl")
            .stock(3)
            .build();

    public static final ProductDto PRODUCT_DTO_EXAMPLE = ProductDto.builder()
            .id(1L)
            .title("bag")
            .price(22.5)
            .description("description")
            .image("imageurl")
            .stock(3)
            .build();


    //    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(nullable = false, unique = true, updatable = false)
//    private Long id;
//
//    @Column(nullable = false, unique = true)
//    private String title;
//
//    @Column(nullable = false)
//    private double price;
//
//    @Column(nullable = false, length = 1000)
//    private String description;
//
//    @ManyToOne
//    @JoinColumn(name = "category_id_fk")
//    private Category category;
//
//    @Column(nullable = false)
//    private String image;
//
//    @Column(nullable = false)
//    private int stock;


}
