package com.trodix.demo.domain.model;

import com.trodix.demo.domain.search.entity.Entity;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class Product extends Entity {

    private Long id;
    private String title;
    private String description;
    private String category;
    private Double price;
    private Double discountPercentage;
    private Double rating;
    private Long stock;
    private List<String> tags;
    private String brand;
    private String sku;
    private Long weight;
    private Dimensions dimensions;
    private String warrantyInformation;
    private String shippingInformation;
    private String availabilityStatus;
    private List<Review> reviews;
    private String returnPolicy;
    private Long minimumOrderQuantity;
    private Meta meta;
    private List<String> images;
    private String thumbnail;

    @Data
    public static class Dimensions {
        private Double width;
        private Double height;
        private Double depth;
    }

    @Data
    public static class Review {
        private int rating;
        private String comment;
        private OffsetDateTime date;
        private String reviewerName;
        private String reviewerEmail;
    }

    @Data
    public static class Meta {
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private String barcode;
        private String qrcode;
    }

}
