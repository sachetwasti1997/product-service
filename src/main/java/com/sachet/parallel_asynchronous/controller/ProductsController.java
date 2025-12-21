package com.sachet.parallel_asynchronous.controller;

import com.sachet.parallel_asynchronous.configuration.EnvironmentConfiguration;
import com.sachet.parallel_asynchronous.model.Product;
import com.sachet.parallel_asynchronous.model.ServerResponse;
import com.sachet.parallel_asynchronous.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class ProductsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductsController.class);

    private final ProductService productService;

    public ProductsController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/get-product")
    public List<Product> getProduct() {
        return productService.getAndSaveProduct();
    }
}
