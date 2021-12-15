package com.tokoped.webscraping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.concurrent.ExecutionException;



@SpringBootApplication
public class WebscrapingTokopedApplication {



	public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
		ParalllelWebScraping.webScraping();
		SpringApplication.run(WebscrapingTokopedApplication.class, args);
	}
}
