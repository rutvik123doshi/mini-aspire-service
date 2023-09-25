package com.rutvik.interview.aspire;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = {HibernateJpaAutoConfiguration.class })
public class DatabaseMain implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DatabaseMain.class, args);
	}



	@Override
	public void run(String... args) {
	}
}
