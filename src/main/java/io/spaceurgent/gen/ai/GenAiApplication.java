package io.spaceurgent.gen.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class GenAiApplication {

	static void main(String[] args) {
		SpringApplication.run(GenAiApplication.class, args);
	}

	static  {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
}
