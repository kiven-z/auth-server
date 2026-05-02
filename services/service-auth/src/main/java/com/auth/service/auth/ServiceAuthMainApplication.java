package com.auth.service.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证服务启动类
 *
 * @author Bunny
 */
@SpringBootApplication
@EnableFeignClients
public class ServiceAuthMainApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceAuthMainApplication.class, args);
	}

}
