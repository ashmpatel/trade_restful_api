package com.example.springboot;

import com.example.storageservice.StorageService;
import com.example.utils.ProductProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackageClasses = ProductProvider.class)
@ComponentScan(basePackages = {"com.example"})
public class Application {

	Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(StorageService storageService) {
		return args -> {
			storageService.deleteAll();
			storageService.init();
			logger.info("File storage service initialized");
		};
	}


}
