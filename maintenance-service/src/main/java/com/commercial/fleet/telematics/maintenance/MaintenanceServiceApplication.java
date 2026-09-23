package com.commercial.fleet.telematics.maintenance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
public class MaintenanceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MaintenanceServiceApplication.class, args);
	}

}
