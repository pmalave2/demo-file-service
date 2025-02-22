package com.demos.file_service;

import org.springframework.boot.SpringApplication;

public class TestFileServiceApplication {

	public static final String PROFILE = "test";

	public static void main(String[] args) {
		SpringApplication.from(FileServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
