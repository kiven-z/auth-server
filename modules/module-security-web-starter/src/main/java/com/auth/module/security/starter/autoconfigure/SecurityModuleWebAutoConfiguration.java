package com.auth.module.security.starter.autoconfigure;

import com.auth.module.security.starter.config.MybatisPlusConfig;
import com.auth.module.security.starter.exception.SecurityExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Security Module Web 自动装配
 *
 * @author Bunny
 */
@Import({ MybatisPlusConfig.class,
		// 异常捕获
		SecurityExceptionHandler.class, })
@Configuration
public class SecurityModuleWebAutoConfiguration {

}
