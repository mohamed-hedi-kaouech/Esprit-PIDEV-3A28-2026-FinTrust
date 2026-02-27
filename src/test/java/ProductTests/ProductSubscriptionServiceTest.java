package ProductTests;


import org.example.Model.Product.ClassProduct.Product;
import org.example.Model.Product.ClassProduct.ProductSubscription;
import org.example.Model.Product.EnumProduct.ProductCategory;
import org.example.Model.Product.EnumProduct.SubscriptionStatus;
import org.example.Model.Product.EnumProduct.SubscriptionType;
import org.example.Service.ProductService.ProductService;
import org.example.Service.ProductService.ProductSubscriptionService;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductSubscriptionServiceTest {

    static ProductSubscriptionService service;
    static ProductSubscription testSubscription;
    static int insertedId;



    @BeforeAll
    static void setup() {
        ProductService productService = new ProductService();
        Product testProduct = new Product();
        testProduct.setCategory(ProductCategory.CARTE_DEBIT);
        testProduct.setPrice(100.0);
        testProduct.setDescription("Produit test pour subscription");
        testProduct.setCreatedAt(LocalDateTime.now());
        productService.add(testProduct);

        // Récupérer l'ID du produit ajouté
        List<Product> products = productService.ReadAll();
        Product last = products.get(products.size() - 1);
        int productId = last.getProductId();

        service = new ProductSubscriptionService();
        testSubscription = new ProductSubscription();
        testSubscription.setClient(1);
        testSubscription.setProduct(productId);
        testSubscription.setType(SubscriptionType.MONTHLY);
        testSubscription.setSubscriptionDate(LocalDateTime.now());
        testSubscription.setExpirationDate(LocalDateTime.now().plusMonths(1));
        testSubscription.setStatus(SubscriptionStatus.DRAFT);
    }

    @Test
    @Order(1)
    void testAdd() {
        service.Add(testSubscription);

        List<ProductSubscription> list = service.ReadAll();
        ProductSubscription last = list.get(list.size() - 1);

        insertedId = last.getSubscriptionId(); // on garde l'ID pour cleanup
        assertNotNull(last);
        assertEquals(testSubscription.getClient(), last.getClient());
    }

    @Test
    @Order(2)
    void testReadId() {
        ProductSubscription ps = service.ReadId(insertedId);
        assertNotNull(ps);
        assertEquals(insertedId, ps.getSubscriptionId());
    }

    @Test
    @Order(3)
    void testUpdate() {
        ProductSubscription ps = service.ReadId(insertedId);
        ps.setStatus(SubscriptionStatus.ACTIVE);
        service.Update(ps);

        ProductSubscription updated = service.ReadId(insertedId);
        assertEquals(SubscriptionStatus.ACTIVE, updated.getStatus());
    }

    @Test
    @Order(4)
    void testGetParClient() {
        List<ProductSubscription> subs = service.getParClient(1);
        assertTrue(subs.stream().anyMatch(s -> s.getSubscriptionId() == insertedId));
    }

    @AfterAll
    static void cleanup() {
        if (insertedId != 0) {
            service.Delete(insertedId);
            System.out.println("🧹 ProductSubscription de test supprimée !");
        }
    }
}
