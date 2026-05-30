package com.auth.common.data.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * MyBatisPlus 基础配置, 包含分页插件、乐观锁插件、防止全表删除插件
 *
 * @author Bunny
 */
public interface BaseMybatisPlusConfig extends MetaObjectHandler {

	/**
	 * 配置并注入 MyBatis Plus 拦截器
	 * @return 拦截器
	 */
	@Bean
	default MybatisPlusInterceptor mybatisPlusInterceptor() {
		// 拦截器
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

		// 使用分页插件
		PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
		paginationInnerInterceptor.setDbType(DbType.MYSQL);
		// 使用优化COUNT，如果是大数据量表会使用估算值
		paginationInnerInterceptor.setOptimizeJoin(false);
		paginationInnerInterceptor.setMaxLimit(600L);
		interceptor.addInnerInterceptor(paginationInnerInterceptor);

		// 乐观锁
		interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

		// 防止全表删除
		interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

		return interceptor;
	}

}
