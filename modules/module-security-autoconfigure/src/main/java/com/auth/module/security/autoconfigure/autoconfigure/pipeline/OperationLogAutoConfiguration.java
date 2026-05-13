package com.auth.module.security.autoconfigure.autoconfigure.pipeline;

import com.auth.module.security.autoconfigure.audit.OperationLogAspect;
import com.auth.module.security.autoconfigure.service.NoopOperationLogHandler;
import com.auth.module.security.contract.spi.OperationLogHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 操作日志 SPI 兜底与 AOP 装配。
 *
 * @author Bunny
 */
@Configuration(proxyBeanMethods = false)
@EnableAspectJAutoProxy
@ConditionalOnBooleanProperty(prefix = "auth.common.operation-log", name = "enabled", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class OperationLogAutoConfiguration {

	/**
	 * 空操作日志处理器。
	 */
	@Bean
	@ConditionalOnMissingBean(OperationLogHandler.class)
	NoopOperationLogHandler noopOperationLogHandler() {
		return new NoopOperationLogHandler();
	}

	/**
	 * 操作日志切面（依赖 Jackson {@link ObjectMapper}）。
	 */
	@Bean
	@ConditionalOnBean(ObjectMapper.class)
	OperationLogAspect operationLogAspect(OperationLogHandler operationLogHandler, ObjectMapper objectMapper) {
		return new OperationLogAspect(operationLogHandler, objectMapper);
	}

}
