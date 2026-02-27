package org.example.Model.Product.ClassProduct;

import org.example.Model.Product.EnumProduct.ProductCategory;

import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class Product {

    //Attributes
    private int productId;
    private ProductCategory category;
    private Double price;
    private String description;
    private LocalDateTime createdAt;


    //Constructors
    public Product() {
        this.createdAt = LocalDateTime.now();
    }

    public Product( ProductCategory category, String description, Double price) {
        this.category = category;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.price = price;
    }
    public Product(int productId, ProductCategory category, String description, Double price) {
        this.productId = productId;
        this.category = category;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.price = price;
    }


    //Getters & Setters

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }



    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", category=" + category +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;

        return productId == product.productId;
    }

    public static String SQLTable() {
        return """
                CREATE TABLE IF NOT EXISTS product (
                  productId INT NOT NULL AUTO_INCREMENT,
                  category ENUM(
                    'COMPTE_COURANT',
                    'COMPTE_EPARGNE',
                    'COMPTE_PREMIUM',
                    'COMPTE_JEUNE',
                    'COMPTE_ENTREPRISE',
                    'CARTE_DEBIT',
                    'CARTE_CREDIT',
                    'CARTE_PREMIUM',
                    'CARTE_VIRTUELLE',
                    'EPARGNE_CLASSIQUE',
                    'EPARGNE_LOGEMENT',
                    'DEPOT_A_TERME',
                    'PLACEMENT_INVESTISSEMENT',
                    'ASSURANCE_VIE',
                    'ASSURANCE_HABITATION',
                    'ASSURANCE_VOYAGE'
                  ) NOT NULL DEFAULT 'COMPTE_COURANT',
                  price DOUBLE NOT NULL,
                  description VARCHAR(500) NOT NULL,
                  createdAt DATE NOT NULL,
                  PRIMARY KEY (productId)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;}


}
