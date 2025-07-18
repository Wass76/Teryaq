package com.Teryaq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware2")
@EnableAspectJAutoProxy
@EnableCaching
@EnableAsync
@EnableWebSecurity
public class TeryaqApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeryaqApplication.class, args);
	}
}
