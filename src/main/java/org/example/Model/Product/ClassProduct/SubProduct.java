package org.example.Model.Product.ClassProduct;

import org.example.Model.Product.EnumProduct.ProductCategory;
import org.example.Model.Product.EnumProduct.SubscriptionStatus;
import org.example.Model.Product.EnumProduct.SubscriptionType;

import java.time.LocalDate;

public class SubProduct {

    private int subscriptionId;
    private SubscriptionType type;
    private LocalDate subscriptionDate;
    private LocalDate expirationDate;
    private SubscriptionStatus status;

    private int productId;
    private ProductCategory category;
    private double price;
    private String description;


    public SubProduct(int subscriptionId, SubscriptionType type,
                      LocalDate subscriptionDate,
                      LocalDate expirationDate,
                      SubscriptionStatus status,
                      int productId,
                      ProductCategory category,
                      double price,
                      String description) {

        this.subscriptionId = subscriptionId;
        this.type = type;
        this.subscriptionDate = subscriptionDate;
        this.expirationDate = expirationDate;
        this.status = status;
        this.productId = productId;
        this.category = category;
        this.price = price;
        this.description = description;
    }

    public int getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(int subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public SubscriptionType getType() {
        return type;
    }

    public void setType(SubscriptionType type) {
        this.type = type;
    }

    public LocalDate getSubscriptionDate() {
        return subscriptionDate;
    }

    public void setSubscriptionDate(LocalDate subscriptionDate) {
        this.subscriptionDate = subscriptionDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
