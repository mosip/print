package io.mosip.print.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("io.mosip.print.*")
public class TestBootApplication {



	
	public static void main(String[] args) {
		SpringApplication.run(TestBootApplication.class, args);
	}
}
