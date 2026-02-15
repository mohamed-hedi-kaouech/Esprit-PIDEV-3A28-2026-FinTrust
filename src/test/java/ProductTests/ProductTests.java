package ProductTests;
import org.example.Model.Product.ClassProduct.Product;
import org.example.Model.Product.EnumProduct.ProductCategory;
import org.example.Service.ProductService.ProductService;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductTests {

    static ProductService productService;
    static Product testProduct;
    static int insertedProductId;

    @BeforeAll
    static void setup() {
        productService = new ProductService();

        testProduct = new Product();
        testProduct.setCategory(ProductCategory.CARTE_CREDIT);
        testProduct.setPrice(150.0);
        testProduct.setDescription("Test Product JUnit");
        testProduct.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @Order(1)
    void testAddProduct() {
        boolean result = productService.add(testProduct);
        assertTrue(result);

        List<Product> products = productService.ReadAll();
        Product lastProduct = products.get(products.size() - 1);
        insertedProductId = lastProduct.getProductId();
    }

    @Test
    @Order(2)
    void testReadAll() {
        List<Product> products = productService.ReadAll();
        assertNotNull(products);
        assertFalse(products.isEmpty());
    }

    @Test
    @Order(3)
    void testUpdateProduct() {
        Product p = productService.ReadId(insertedProductId);
        p.setDescription("Updated by JUnit");

        boolean result = productService.update(p);
        assertTrue(result);
    }

    @AfterAll
    static void cleanup() {
        if (insertedProductId != 0) {
            boolean deleted = productService.delete(insertedProductId);
            if (deleted) {
                System.out.println("Produit de test supprimé avec succès !");
            } else {
                System.out.println("Impossible de supprimer le produit de test !");
            }
        }
    }
}

