package com.team6.team6;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableRetry
@EnableJpaAuditing
@SpringBootApplication
@EnableAsync
public class Team6Application {

	public static void main(String[] args) {
		SpringApplication.run(Team6Application.class, args);
	}

}
