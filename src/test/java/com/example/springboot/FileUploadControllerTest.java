package com.example.springboot;

import com.example.storageservice.StorageProperties;
import com.example.storageservice.StorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FileUploadControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private StorageService storageService;

	@Autowired
	private StorageProperties storageProperties;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private List<Path> filesToBeDeleted = new ArrayList<>();

	@Test
	public void getServerStatus() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("Trade Uploader Is Running")));
	}


	@Test
	public void whenTradeFileUploaded_thenVerifyStatus() throws Exception {
		MockMultipartFile file
				= new MockMultipartFile(
				"file",
				"trade_sample.csv",
				MediaType.TEXT_PLAIN_VALUE,
				"date,product_id,currency,price\n20160101,1,EUR,10".getBytes()
		);

		MockMvc mockMvc
				= MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		mockMvc.perform(multipart("/api/vi/enrich").file(file))
				.andExpect(status().isOk());
		Path rootLocation = Paths.get(storageProperties.getLocation());

		//test when a file is uploaded, then the file actually exists on the server directory
		Path docRootPath = Path.of(rootLocation.toString(), "trade_sample.csv");
		filesToBeDeleted.add(docRootPath);

		// check the trade file was uploaded
		assertEquals(Files.exists(docRootPath), true);
	}


	// Need this as a test run will load the sample trade files so this keeps the dirs clean, repeatable
	@AfterEach
	public void cleanup() {
		filesToBeDeleted.forEach(path -> {
			try {
				Files.deleteIfExists(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

}