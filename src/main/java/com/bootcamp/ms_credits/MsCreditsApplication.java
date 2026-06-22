package com.bootcamp.ms_credits;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MsCreditsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsCreditsApplication.class, args);
	}

}
