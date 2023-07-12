package com.example.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Holds Trade details
 */
public class Trade {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Logger logger = LoggerFactory.getLogger(Trade.class);

    private String parsedDate;
    private Long productId;
    private String currency;
    private BigDecimal price;

    // to indicate if this trade id valid e.g. date was parsed correctly and other conditions
    private boolean valid = true;

    /**
     * When trade data is sent per line, parses it , esp the date then stores it,
     * @param text
     */
    public Trade(String text) {
        if (text.length() > 0) {
            String[] values = text.split(",");
            String parsedDate = parseDate(values[0], text);
            this.parsedDate = parsedDate;
            this.productId = Long.parseLong(values[1]);
            this.currency = values[2];
            if (values[3].indexOf(".") == -1) {
                values[3] = values[3].trim() + ".0";
            }
            ;
            this.price = new BigDecimal(values[3].trim());
            logger.debug("Processed trade data :" + text);

            // check if Trade is valid
            checkIfTradeIsValid(text, parsedDate);
        }
    }

    private void checkIfTradeIsValid(String text, String parsedDate) {
        // the only condition we are checking for is IF trade is valid is the date check
        if (parsedDate.equals("")) {
            logger.debug("This row will be skipped as DATE was not formatted correctly :" + text);
            markTradeAsInvalid();
        }
    }


    private String parseDate(String textDate, String tradeRow) {
        LocalDate parsedDate = null;
        boolean validDate = false;
        String formattedDate = "";

        try {
            parsedDate = LocalDate.parse(textDate, formatter);
            validDate = true;
        } catch (DateTimeParseException e) {
            logger.error("Trade has invalid date " + tradeRow);
            logger.error("The date " + textDate + " could not be parsed");
            validDate = false;
        }

        // if we have a valid date then we can format it otherwise return empty string and log the error and skip this row
        if (validDate) {
            formattedDate = parsedDate.format(formatter);
        } else {
            formattedDate = "";
        }
        return formattedDate;
    }

    public String getParsedDate() {
        return parsedDate;
    }

    public Long getProductId() {
        return productId;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isValidTrade() {
        return this.valid;
    }

    private void markTradeAsInvalid() {
        this.valid = false;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "parsedDate='" + parsedDate + '\'' +
                ", productId=" + productId +
                ", currency='" + currency + '\'' +
                ", price=" + price +
                '}';
    }

}
