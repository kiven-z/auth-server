package com.auth.common.web.autoconfigure;

import com.auth.common.web.config.ControllerStringParamTrimConfig;
import com.auth.common.web.config.WebConfig;
import com.auth.common.web.context.SpringContextHolder;
import com.auth.common.web.exception.FallbackExceptionHandler;
import com.auth.common.web.exception.MyBatisDatabaseExceptionHandler;
import com.auth.common.web.exception.ValidationExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 通用Web 自动装配
 *
 * @author Bunny
 */
@Slf4j
@AutoConfiguration(before = { WebMvcAutoConfiguration.class })
@ConditionalOnBooleanProperty(prefix = "auth.common.web", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties()
@Import({ ControllerStringParamTrimConfig.class, WebConfig.class, SpringContextHolder.class,
		// 导入异常处理器
		ValidationExceptionHandler.class, FallbackExceptionHandler.class, MyBatisDatabaseExceptionHandler.class })
public class CommonWebAutoConfiguration implements ApplicationRunner {

	@Override
	public void run(ApplicationArguments args) {
		log.info("Registering CommonWebAutoConfiguration");
	}

}
