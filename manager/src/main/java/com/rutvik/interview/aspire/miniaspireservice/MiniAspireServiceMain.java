package com.rutvik.interview.aspire.miniaspireservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
@ComponentScan(basePackages = {"com.rutvik.interview.aspire"})
public class MiniAspireServiceMain {

	public static void main(String[] args) {
		SpringApplication.run(MiniAspireServiceMain.class, args);
	}

}
