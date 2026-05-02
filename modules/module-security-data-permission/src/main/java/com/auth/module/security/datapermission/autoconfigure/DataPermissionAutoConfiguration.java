package com.auth.module.security.datapermission.autoconfigure;

import com.auth.module.security.datapermission.context.LoginUserProvider;
import com.auth.module.security.datapermission.context.SecurityContextLoginUserProvider;
import com.auth.module.security.datapermission.interceptor.DataScopeInnerInterceptor;
import com.auth.module.security.datapermission.policy.DefaultUserPolicyProvider;
import com.auth.module.security.datapermission.policy.PolicyDataScopeHandler;
import com.auth.module.security.datapermission.policy.PolicyHandlerRegistry;
import com.auth.module.security.datapermission.policy.UserPolicyProvider;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 数据权限模块自动配置
 *
 * @author Bunny
 */
@AutoConfiguration
@ConditionalOnClass(MybatisPlusInterceptor.class)
public class DataPermissionAutoConfiguration {

	/**
	 * 登录用户提供者
	 * @return 登录用户提供者
	 */
	@ConditionalOnMissingBean
	@Bean
	LoginUserProvider loginUserProvider() {
		return new SecurityContextLoginUserProvider();
	}

	/**
	 * 用户策略提供者
	 * @return 用户策略提供者
	 */
	@ConditionalOnMissingBean
	@Bean
	UserPolicyProvider userPolicyProvider() {
		return new DefaultUserPolicyProvider();
	}

	/**
	 * 策略处理器注册
	 * @param handlers 处理器列表
	 * @return 策略处理器注册
	 */
	@ConditionalOnMissingBean
	@Bean
	PolicyHandlerRegistry policyHandlerRegistry(List<PolicyDataScopeHandler> handlers) {
		return new PolicyHandlerRegistry(handlers);
	}

	/**
	 * 数据范围内部拦截器
	 * @param loginUserProvider 登录用户提供者
	 * @return 数据范围内部拦截器
	 */
	@ConditionalOnMissingBean
	@Bean
	DataScopeInnerInterceptor dataScopeInnerInterceptor(LoginUserProvider loginUserProvider) {
		return new DataScopeInnerInterceptor(loginUserProvider);
	}

	/**
	 * MybatisPlus 拦截器添加数据范围内部拦截器
	 * @param interceptor MybatisPlus 拦截器
	 * @param dataScopeInnerInterceptor 数据范围内部拦截器
	 * @return MybatisPlus 拦截器
	 */
	@Bean
	MybatisPlusInterceptor mybatisPlusInterceptorWithDataScope(MybatisPlusInterceptor interceptor,
			DataScopeInnerInterceptor dataScopeInnerInterceptor) {
		interceptor.addInnerInterceptor(dataScopeInnerInterceptor);
		return interceptor;
	}

}
