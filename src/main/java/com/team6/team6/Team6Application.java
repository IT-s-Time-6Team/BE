package com.team6.team6;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class Team6Application {

	public static void main(String[] args) {
		SpringApplication.run(Team6Application.class, args);
	}

}
