package com.auth.module.security.autoconfigure.boot.pipeline;

import com.auth.common.ip.IpAddressService;
import com.auth.module.security.autoconfigure.audit.operationlog.OperationLogAspect;
import com.auth.module.security.autoconfigure.audit.operationlog.OperationLogPayloadAssembler;
import com.auth.module.security.autoconfigure.service.NoopOperationLogHandler;
import com.auth.module.security.contract.spi.OperationLogHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 操作日志 SPI 兜底与 AOP 装配
 *
 * @author Bunny
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnBooleanProperty(prefix = "auth.common.operation-log", name = "enabled", matchIfMissing = true)
@Configuration(proxyBeanMethods = false)
@EnableAspectJAutoProxy
public class OperationLogAutoConfiguration {

	/**
	 * 空操作日志处理器
	 */
	@ConditionalOnMissingBean(OperationLogHandler.class)
	@Bean
	NoopOperationLogHandler noopOperationLogHandler() {
		return new NoopOperationLogHandler();
	}

	/**
	 * 操作日志负载组装器
	 */
	@ConditionalOnBean(ObjectMapper.class)
	@Bean
	OperationLogPayloadAssembler operationLogPayloadAssembler(
			ObjectProvider<OperationLogHandler> operationLogHandlerProvider, ObjectMapper objectMapper,
			IpAddressService ipAddressService) {
		OperationLogHandler handler = operationLogHandlerProvider.getIfUnique();
		return new OperationLogPayloadAssembler(handler, objectMapper, ipAddressService);
	}

	/**
	 * 操作日志切面
	 */
	@ConditionalOnBean(OperationLogPayloadAssembler.class)
	@Bean
	OperationLogAspect operationLogAspect(OperationLogPayloadAssembler operationLogPayloadAssembler) {
		return new OperationLogAspect(operationLogPayloadAssembler);
	}

}
