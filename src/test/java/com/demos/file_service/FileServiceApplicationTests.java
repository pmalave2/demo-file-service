package com.demos.file_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
class FileServiceApplicationTests {

	@Test
	void contextLoads() { // This test is empty because it is only used to verify that the application context loads successfully
	}

}
