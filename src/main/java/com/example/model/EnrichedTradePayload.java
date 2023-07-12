package com.example.model;

import java.math.BigDecimal;

/**
 * Hold the enriched trade data after looking up the product id in the product map
 */
public class EnrichedTradePayload {

    private String parsedDate;
    private String productName;
    private String currency;
    private BigDecimal price;

    public EnrichedTradePayload(String parsedDate, String productName, String currency, BigDecimal price) {
        this.parsedDate = parsedDate;
        this.productName = productName;
        this.currency = currency;
        this.price = price;
    }

    public String getParsedDate() {
        return parsedDate;
    }

    public String getProductName() {
        return productName;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Used to convert this pojo into a csv representation.
     *
     * @return
     */
    @Override
    public String toString() {
        return parsedDate + "," + productName + "," + currency + "," + price;
    }
}
