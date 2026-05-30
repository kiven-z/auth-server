package com.auth.common.web.listener;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.auth.common.web.model.enums.NacosServiceEnum.AUTH_SERVICE_USER_CONFIG;

/**
 * Nacos user configuration listener. 只是Demo测试使用，如果真的要监听放到业务服务中
 *
 * @author Bunny
 */
@Slf4j
@Component
public class NacosUserConfigLister {

	@Bean
	public ApplicationRunner runner(NacosConfigManager configManager) {
		String serviceFilename = AUTH_SERVICE_USER_CONFIG.getFilename();
		String groupName = AUTH_SERVICE_USER_CONFIG.getGroup();

		return args -> {
			ConfigService configService = configManager.getConfigService();

			configService.addListener(serviceFilename, groupName, new Listener() {
				@Override
				public Executor getExecutor() {
					return Executors.newFixedThreadPool(10);
				}

				@Override
				public void receiveConfigInfo(String configInfo) {
					log.info("Received configuration info: {}", configInfo);
				}
			});

			log.info("Listening for configuration - serviceFilename: {} groupName: {}...", serviceFilename, groupName);
		};
	}

}
