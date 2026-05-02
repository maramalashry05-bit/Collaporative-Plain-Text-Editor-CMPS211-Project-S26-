package com.texteditor.apt.Networking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.texteditor.apt")
@EnableJpaRepositories(basePackages = "com.texteditor.apt.Document")
@EntityScan(basePackages = "com.texteditor.apt.Document")
public class AptApplication {

	public static void main(String[] args) {
		SpringApplication.run(AptApplication.class, args);
	}

}
