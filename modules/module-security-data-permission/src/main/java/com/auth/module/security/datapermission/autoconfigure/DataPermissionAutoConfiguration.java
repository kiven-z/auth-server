package com.auth.module.security.datapermission.autoconfigure;

import com.auth.module.security.datapermission.context.LoginUserProvider;
import com.auth.module.security.datapermission.context.SecurityContextLoginUserProvider;
import com.auth.module.security.datapermission.handler.DataScopePermissionHandler;
import com.auth.module.security.datapermission.support.DataScopeAnnotationResolver;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据权限模块自动配置
 *
 * <p>
 * 将 {@link DataPermissionInterceptor} 插到分页插件之前，保证 count/page SQL 都带上范围条件。
 * </p>
 *
 * @author Bunny
 */
@AutoConfiguration
@ConditionalOnClass({ MybatisPlusInterceptor.class, DataPermissionInterceptor.class })
public class DataPermissionAutoConfiguration {

	/**
	 * 登录用户提供者（允许业务侧覆盖）
	 * @return 登录用户提供者
	 */
	@ConditionalOnMissingBean
	@Bean
	LoginUserProvider loginUserProvider() {
		return new SecurityContextLoginUserProvider();
	}

	/**
	 * 将数据权限拦截器插入插件链最前（位于分页之前）
	 * @param interceptor MyBatis-Plus 拦截器
	 * @param loginUserProvider 登录用户提供者
	 * @return 同一拦截器实例
	 */
	@Bean
	MybatisPlusInterceptor mybatisPlusInterceptorWithDataScope(MybatisPlusInterceptor interceptor,
			LoginUserProvider loginUserProvider) {
		DataScopeAnnotationResolver annotationResolver = new DataScopeAnnotationResolver();
		DataScopePermissionHandler dataPermissionHandler = new DataScopePermissionHandler(loginUserProvider,
				annotationResolver);
		DataPermissionInterceptor dataPermission = new DataPermissionInterceptor(dataPermissionHandler);

		List<InnerInterceptor> ordered = new ArrayList<>(interceptor.getInterceptors().size() + 1);
		ordered.add(dataPermission);
		ordered.addAll(interceptor.getInterceptors());
		interceptor.setInterceptors(ordered);
		return interceptor;
	}

}
