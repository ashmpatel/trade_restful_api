package com.example.utils;


import com.example.model.EnrichedTradePayload;
import com.example.model.Product;
import com.example.model.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is the callback that gets called by the mem mapped file reader each time a row is successfully read.
 */
public class ProcessTradeData implements CallBackListener {

    private static final Logger logger = LoggerFactory.getLogger(ProcessTradeData.class);

    private Map<Long, Product> productMap;
    private List<EnrichedTradePayload> processedTrades = new ArrayList<>();

    private static final String PRODUCT_LOOKUP_ERROR = "Missing Product Name";

    public ProcessTradeData(Map productMap) {
        this.productMap = productMap;
    }

    @Override
    public EnrichedTradePayload callBack(String data) {
        EnrichedTradePayload enrichedTrade = null;

        Trade parsedTrade = new Trade(data);
        Product prd = productMap.get(parsedTrade.getProductId());
        String productName = "";

        // lookup the product Id and IF NOT in the map ,then as per the spec , return the string : Missing Product Name
        if (prd == null) {
            productName = PRODUCT_LOOKUP_ERROR;
        } else {
            productName = prd.getProductName();
        }

        // create the Enriched Trade Payload and add it to the result set
        // NOTE the trade was invalid e.g invalid date then SKIP this trade row as per the requirement AND log an error
        if (parsedTrade.isValidTrade()) {
            enrichedTrade = new EnrichedTradePayload(parsedTrade.getParsedDate(), productName, parsedTrade.getCurrency(), parsedTrade.getPrice());
            processedTrades.add(enrichedTrade);
            logger.info("Processed trade : " + enrichedTrade);
        } else {
            logger.error("The trade is NOT valid : " + parsedTrade);
        }

        return enrichedTrade;
    }

    public List<EnrichedTradePayload> getProcessedTrades() {
        return processedTrades;
    }

}
