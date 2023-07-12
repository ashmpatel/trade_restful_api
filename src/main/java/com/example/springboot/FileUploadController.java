package com.example.springboot;

import com.example.model.Product;
import com.example.processor.TradeProcessor;
import com.example.storageservice.StorageFileNotFoundException;
import com.example.storageservice.StorageService;
import com.example.utils.ProductProvider;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
public class FileUploadController {
	Logger logger = LoggerFactory.getLogger(FileUploadController.class);

	private ProductProvider productList;
	private TradeProcessor tradeProcessor;

	private Map<Long, Product> productMap = Collections.emptyMap();

	private final StorageService storageService;

	@Autowired
	public FileUploadController(StorageService storageService, ProductProvider productList , TradeProcessor tradeProcessor) throws IOException {
		this.storageService = storageService;
		this.productList = productList;
		this.productList.readProducts();
		this.tradeProcessor = tradeProcessor;

	}

	@PostConstruct
	public void loadProductsonStartup() {
		productMap = productList.getProductMap();
		logger.info("Loaded products : " + productMap.size());
	}


	@PostMapping("/api/vi/enrich")
	public String handleFileUpload(@RequestParam("file") MultipartFile file,
								   RedirectAttributes redirectAttributes) throws IOException {

		storageService.store(file);
		String tradeResults = tradeProcessor.processTrades(file.getOriginalFilename());

		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "!");

		return tradeResults;
	}

	@GetMapping("/")
	public String index() {
		return "Trade Uploader Is Running";
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}
