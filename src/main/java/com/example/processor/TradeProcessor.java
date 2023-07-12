package com.example.processor;

import com.example.model.EnrichedTradePayload;
import com.example.storageservice.StorageService;
import com.example.utils.ProcessTradeData;
import com.example.utils.ProductProvider;
import com.example.utils.ReadCsv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

@Component
public class TradeProcessor {

    // the header for the return results. We need to know the column names for the csv file returned
    private final static String HEADER = "date,product_name,currency,price";

    private final static String NEWLINE_SEPARATOR = "\n";

    private StorageService storageService;
    private ProductProvider productMapProvider;

    @Autowired
    public TradeProcessor(StorageService storageService, ProductProvider productMapProvider) {
        this.storageService = storageService;
        this.productMapProvider = productMapProvider;
    }

    /**
     * Gets the product map from the stored file, passes that to the trade call back handler and then processes each
     * trade line by line. Finally, converts All the processed data to CSV , no streaming in this version.
     * @return
     */
    public String processTrades(String fileName) {

        // hold the results per part file processed
        List<String> csvResults = new LinkedList<>();
        csvResults.add(HEADER);
        Path tradeFile = storageService.load(fileName);

        if (tradeFile!=null) {
            // read the csv file and process it with the product map lookup
            ProcessTradeData tradeCallbackHandler = new ProcessTradeData(productMapProvider.getProductMap());
            ReadCsv processCsvTrades = new ReadCsv(tradeCallbackHandler);
            processCsvTrades.read(tradeFile.toUri().getPath());
            List<EnrichedTradePayload> tradeResults = tradeCallbackHandler.getProcessedTrades();
            for (EnrichedTradePayload t : tradeResults) {
                csvResults.add(t.toString());
            }
        }


        // return the results as the data comes in and is processed
        return String.join(NEWLINE_SEPARATOR, csvResults);
    }



}