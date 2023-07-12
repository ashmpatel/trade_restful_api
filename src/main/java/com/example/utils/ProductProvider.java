package com.example.utils;

import com.example.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Component
public class ProductProvider {
    Logger logger = LoggerFactory.getLogger(ProductProvider.class);

    private final static String PRODUCT_LOOKUP_DATA= "classpath:products.csv";

    private ListenForProductData productListener = new ListenForProductData();
    private Map<Long, Product> productMap;

    public void readProducts() throws IOException {
        File file = ResourceUtils.getFile(PRODUCT_LOOKUP_DATA);
        ReadCsv productsData = new ReadCsv(productListener);
        productsData.read(file.getCanonicalPath());
        productMap = productListener.getProducts();
        logger.info("Read all the products");
    }

    public Map<Long, Product> getProductMap() {
        return productMap;
    }

}
