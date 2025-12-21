package com.sachet.parallel_asynchronous;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ParallelAsynchronousApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParallelAsynchronousApplication.class, args);
	}

}
