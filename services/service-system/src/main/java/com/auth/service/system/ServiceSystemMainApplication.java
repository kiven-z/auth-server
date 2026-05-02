package com.auth.service.system;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 系统服务启动类
 *
 * @author Bunny
 */
@EnableFileStorage
@SpringBootApplication
@EnableFeignClients
public class ServiceSystemMainApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceSystemMainApplication.class, args);
	}

}
