package com.auth.module.security.autoconfig.autoconfigure;

import com.auth.common.jwt.autoconfigure.JwtAutoConfiguration;
import com.auth.module.security.autoconfig.autoconfigure.feign.SecurityFeignAutoConfiguration;
import com.auth.module.security.autoconfig.autoconfigure.pipeline.SecurityAuthenticationPipelineConfiguration;
import com.auth.module.security.autoconfig.autoconfigure.validation.SecurityAnnotationConflictAutoConfiguration;
import com.auth.module.security.autoconfig.autoconfigure.web.ResourceServerSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 安全启动器引入当前所有的配置信息
 *
 * @author Bunny
 */
@AutoConfiguration(after = { WebMvcAutoConfiguration.class, JwtAutoConfiguration.class })
@Import({
		// 资源配置包含 SpringSecurity 配置
		ResourceServerSecurityAutoConfiguration.class,
		// Feign 内部调用内部拦截器
		SecurityFeignAutoConfiguration.class,
		// 内部注解定义等
		SecurityAuthenticationPipelineConfiguration.class,
		// 启动期安全注解冲突校验自动配置
		SecurityAnnotationConflictAutoConfiguration.class, })
public class ModuleSecurityAutoConfiguration {

}
