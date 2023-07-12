package com.example.springboot;

import com.example.model.EnrichedTradePayload;
import com.example.model.Product;
import com.example.storageservice.StorageService;
import com.example.utils.ListenForProductData;
import com.example.utils.ProcessTradeData;
import com.example.utils.ReadCsv;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProcessingTests {

    private final static String PRODUCT_LOOKUP_DATA = "classpath:products.csv";

    private StorageService storageService;

    @Autowired
    public ProcessingTests(StorageService storageService) {
        this.storageService = storageService;
    }


    @Test
    public void producesReadAndCountIsCorrect() throws Exception {
        File file = ResourceUtils.getFile(PRODUCT_LOOKUP_DATA);
        ListenForProductData productListener = new ListenForProductData();
        ReadCsv productsData = new ReadCsv(productListener);
        productsData.read(file.getCanonicalPath());
        // all the products in the csv. Spec does not say we can not hold this lookup data in mem so for now, I will keep it in mem.
        // It would be easy enough to put a Stream on top of this so the processing itself is all a Stream when trades come in.
        final Map<Long, Product> productMap = productListener.getProducts();
        assert (productMap.size() == 10);
    }

    @Test
    public void producesReadAndAllEntriesExist() throws Exception {
        File file = ResourceUtils.getFile(PRODUCT_LOOKUP_DATA);
        ListenForProductData productListener = new ListenForProductData();
        ReadCsv productsData = new ReadCsv(productListener);
        productsData.read(file.getCanonicalPath());
        // all the products in the csv. Spec does not say we can not hold this lookup data in mem so for now, I will keep it in mem.
        // It would be easy enough to put a Stream on top of this so the processing itself is all a Stream when trades come in.
        final Map<Long, Product> productMap = productListener.getProducts();
        Long key = Long.valueOf(1);
        Product test1 = productMap.get(key);
        assert (test1.getProductName().trim().equals("Treasury Bills Domestic"));
    }

    @Test
    public void processTradeDateCheckEnrichedTradeDataIsCorrect() throws Exception {
        String TRADE_LOOKUP_DATA = "src/test/resources/trades.csv";

        //get the product map
        File file = ResourceUtils.getFile(PRODUCT_LOOKUP_DATA);
        ListenForProductData productListener = new ListenForProductData();
        ReadCsv productsData = new ReadCsv(productListener);
        productsData.read(file.getCanonicalPath());
        Map<Long, Product> productMap = productListener.getProducts();

        // setup trade processor
        ProcessTradeData tradeCallbackHandler = new ProcessTradeData(productMap);
        ReadCsv processCsvTrades = new ReadCsv(tradeCallbackHandler);

        // get the sample trade data from ../test/resources
        File file2 = new File(TRADE_LOOKUP_DATA);
        Path tradeFile = storageService.load(file2.getCanonicalPath());

        // process trades
        processCsvTrades.read(tradeFile.toUri().getPath());
        List<EnrichedTradePayload> tradeResults = tradeCallbackHandler.getProcessedTrades();
        EnrichedTradePayload first = tradeResults.get(0);
        assert (first.toString().equals("20160101,Treasury Bills Domestic,EUR,10.0"));

    }

    @Test
    public void processTradeDataWithDateIssue() throws Exception {
        String TRADE_LOOKUP_DATA = "src/test/resources/trades_with_date_issue.csv";

        //get the product map
        File file = ResourceUtils.getFile(PRODUCT_LOOKUP_DATA);
        ListenForProductData productListener = new ListenForProductData();
        ReadCsv productsData = new ReadCsv(productListener);
        productsData.read(file.getCanonicalPath());
        Map<Long, Product> productMap = productListener.getProducts();

        // setup trade processor
        ProcessTradeData tradeCallbackHandler = new ProcessTradeData(productMap);
        ReadCsv processCsvTrades = new ReadCsv(tradeCallbackHandler);

        // get the sample trade data from ../test/resources
        File file2 = new File(TRADE_LOOKUP_DATA);
        Path tradeFile = storageService.load(file2.getCanonicalPath());

        // process trades
        processCsvTrades.read(tradeFile.toUri().getPath());
        List<EnrichedTradePayload> tradeResults = tradeCallbackHandler.getProcessedTrades();
        EnrichedTradePayload oneTrade = tradeResults.get(0);
        //only 1 trade was processed as one has an error
        assert (tradeResults.size() == 1 && oneTrade.getParsedDate().equals("20160101") && oneTrade.getPrice().equals(new BigDecimal("20.1")));
    }

    @Test
    public void processEmptyTradeFileAndNoExceptionThrown() throws Exception {
        Exception ex = null;
        String TRADE_LOOKUP_DATA = "src/test/resources/empty_trade_file.csv";

        //get the product map
        File file = ResourceUtils.getFile(PRODUCT_LOOKUP_DATA);
        ListenForProductData productListener = new ListenForProductData();
        ReadCsv productsData = new ReadCsv(productListener);
        productsData.read(file.getCanonicalPath());
        Map<Long, Product> productMap = productListener.getProducts();

        // setup trade processor
        ProcessTradeData tradeCallbackHandler = new ProcessTradeData(productMap);
        ReadCsv processCsvTrades = new ReadCsv(tradeCallbackHandler);

        // no exception is thrown when processing empty trade file
        try {
            // get the sample trade data from ../test/resources
            File file2 = new File(TRADE_LOOKUP_DATA);
            Path tradeFile = storageService.load(file2.getCanonicalPath());

            // process trades
            processCsvTrades.read(tradeFile.toUri().getPath());
            List<EnrichedTradePayload> tradeResults = tradeCallbackHandler.getProcessedTrades();
        } catch (Exception e) {
            ex = e;
        }
        assertEquals(null, ex);

    }

    @Test
    public void processEmptyProductFileAndEmptyTradeFileAndNoExceptionThrown() throws Exception {
        Exception ex = null;
        String TRADE_LOOKUP_DATA = "src/test/resources/empty_trade_file.csv";
        String EMPTY_PRODUCT_LOOKUP_DATA = "src/test/resources/empty_product_file.csv";
        Map<Long, Product> productMap = Collections.emptyMap();

        //get the EMPTY product map and check no exception is thrown
        try {
            File file = ResourceUtils.getFile(EMPTY_PRODUCT_LOOKUP_DATA);
            ListenForProductData productListener = new ListenForProductData();
            ReadCsv productsData = new ReadCsv(productListener);
            productsData.read(file.getCanonicalPath());
            productMap = productListener.getProducts();
        } catch (Exception e) {
            ex = e;
        }

        // setup trade processor
        ProcessTradeData tradeCallbackHandler = new ProcessTradeData(productMap);
        ReadCsv processCsvTrades = new ReadCsv(tradeCallbackHandler);

        // no exception is thrown when processing empty trade file

        // get the sample trade data from ../test/resources
        File file2 = new File(TRADE_LOOKUP_DATA);
        Path tradeFile = storageService.load(file2.getCanonicalPath());
        // process trades
        processCsvTrades.read(tradeFile.toUri().getPath());

        assertEquals(null, ex);

    }

    @Test
    public void processEmptyProductFileAndEmptyTradeFileAndResultsAreZeroTrades() throws Exception {
        Exception ex = null;
        String TRADE_LOOKUP_DATA = "src/test/resources/empty_trade_file.csv";
        String EMPTY_PRODUCT_LOOKUP_DATA = "src/test/resources/empty_product_file.csv";
        Map<Long, Product> productMap = Collections.emptyMap();

        //get the EMPTY product map and check no exception is thrown

        File file = ResourceUtils.getFile(EMPTY_PRODUCT_LOOKUP_DATA);
        ListenForProductData productListener = new ListenForProductData();
        ReadCsv productsData = new ReadCsv(productListener);
        productsData.read(file.getCanonicalPath());
        productMap = productListener.getProducts();


        // setup trade processor
        ProcessTradeData tradeCallbackHandler = new ProcessTradeData(productMap);
        ReadCsv processCsvTrades = new ReadCsv(tradeCallbackHandler);

        // no exception is thrown when processing empty trade file

        // get the sample trade data from ../test/resources
        File file2 = new File(TRADE_LOOKUP_DATA);
        Path tradeFile = storageService.load(file2.getCanonicalPath());
        // process trades
        processCsvTrades.read(tradeFile.toUri().getPath());
        List<EnrichedTradePayload> tradeResults = tradeCallbackHandler.getProcessedTrades();

        assertEquals(tradeResults.size(), 0);

    }

    @Test
    public void processEmptyProductFileAndNoExceptionThrownProcessingTrades() throws Exception {
        Exception ex = null;
        String TRADE_LOOKUP_DATA = "src/test/resources/trades.csv";

        //Test with EMPTY PRODUCT NOT EVEN A HEADER ROW
        Map<Long, Product> productMap = Collections.emptyMap();

        List<EnrichedTradePayload> tradeResults = Collections.EMPTY_LIST;

        try {
            // setup trade processor
            ProcessTradeData tradeCallbackHandler = new ProcessTradeData(productMap);
            ReadCsv processCsvTrades = new ReadCsv(tradeCallbackHandler);

            // no exception is thrown when processing empty trade file

            // get the sample trade data from ../test/resources
            File file2 = new File(TRADE_LOOKUP_DATA);
            Path tradeFile = storageService.load(file2.getCanonicalPath());
            // process trades
            processCsvTrades.read(tradeFile.toUri().getPath());

        } catch (Exception e) {
            ex = e;
        }

        assertEquals(null, ex);
    }

    @Test
    public void processEmptyProductFileTradesHaveMissingProductNameForTheProduct() throws Exception {
        Exception ex = null;
        String TRADE_LOOKUP_DATA = "src/test/resources/trades.csv";
        Map<Long, Product> productMap = Collections.emptyMap();
        List<EnrichedTradePayload> tradeResults = Collections.EMPTY_LIST;

        // setup trade processor
        ProcessTradeData tradeCallbackHandler = new ProcessTradeData(productMap);
        ReadCsv processCsvTrades = new ReadCsv(tradeCallbackHandler);

        // get the sample trade data from ../test/resources
        File file2 = new File(TRADE_LOOKUP_DATA);
        Path tradeFile = storageService.load(file2.getCanonicalPath());
        // process trades
        processCsvTrades.read(tradeFile.toUri().getPath());
        tradeResults = tradeCallbackHandler.getProcessedTrades();

        boolean testTrade1HasMissingProductName = tradeResults.get(0).getProductName().equals("Missing Product Name");
        boolean testTrade2HasMissingProductName = tradeResults.get(1).getProductName().equals("Missing Product Name");

        // both trades have missing product names as expected as no product name was suppl;ied
        assertEquals(testTrade1HasMissingProductName && testTrade2HasMissingProductName, true);

    }


}
