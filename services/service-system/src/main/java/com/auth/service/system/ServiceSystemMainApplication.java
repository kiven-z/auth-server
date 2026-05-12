package com.auth.service.system;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 系统服务启动类
 *
 * @author Bunny
 */
@EnableFileStorage
@SpringBootApplication
@EnableFeignClients
@EnableAsync
public class ServiceSystemMainApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceSystemMainApplication.class, args);
	}

}
