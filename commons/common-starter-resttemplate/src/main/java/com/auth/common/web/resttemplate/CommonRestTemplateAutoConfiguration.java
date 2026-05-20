package com.auth.common.web.resttemplate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置
 *
 * @author Bunny
 */
@Slf4j
@AutoConfiguration
public class CommonRestTemplateAutoConfiguration implements ApplicationRunner {

	@Override
	public void run(ApplicationArguments args) {
		log.info("Common RestTemplate initialized successfully");
	}

	/**
	 * 普通 RestTemplate：用于调用外部 API（如邮件、地图、第三方接口） 明确指定 Bean 名称，并作为主要的候选者
	 */
	@ConditionalOnMissingBean(name = "restTemplate")
	@Bean
	@Primary
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	/**
	 * 负载均衡 RestTemplate：用于微服务间调用 必须配合 @LoadBalanced
	 */
	@ConditionalOnMissingBean(name = "loadBalancedRestTemplate")
	@LoadBalanced
	@Bean
	public RestTemplate loadBalancedRestTemplate() {
		return new RestTemplate();
	}

}
