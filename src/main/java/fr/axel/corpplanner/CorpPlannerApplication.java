package fr.axel.corpplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CorpPlannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CorpPlannerApplication.class, args);
	}

}
