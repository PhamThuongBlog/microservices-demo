package vhbtpm.k22a.catalog_service;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    @GetMapping("/products")
    public Map<String, Object> getProducts(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "guest") String userId) {

        var products = List.of(
            Map.of("id", 1, "name", "Laptop Dell XPS 15", "price", 35000000),
            Map.of("id", 2, "name", "Bàn phím cơ Keychron K2", "price", 1850000),
            Map.of("id", 3, "name", "Màn hình LG UltraGear 27\"", "price", 6900000)
        );

        return Map.of(
            "requestedBy", userId,
            "total", products.size(),
            "items", products
        );
    }
}