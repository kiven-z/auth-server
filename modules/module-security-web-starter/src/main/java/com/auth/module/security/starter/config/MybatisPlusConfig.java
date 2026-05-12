package com.auth.module.security.starter.config;

import com.auth.common.data.config.BaseMybatisPlusConfig;
import com.auth.common.data.model.enums.DatabaseAuditMetaFieldEnum;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;

/**
 * MyBatisPlus 配置自动填充字段 使用 auth.common.mybatis-plus.auto-fill = false 禁用
 *
 * @author Bunny
 */
@ConditionalOnMissingBean(MybatisPlusConfig.class)
@Configuration
@EnableTransactionManagement
public class MybatisPlusConfig implements BaseMybatisPlusConfig {

	private static final String CREATE_TIME = DatabaseAuditMetaFieldEnum.CREATE_TIME.getColumnName();

	private static final String UPDATE_TIME = DatabaseAuditMetaFieldEnum.UPDATE_TIME.getColumnName();

	private static final String CREATE_USER = DatabaseAuditMetaFieldEnum.CREATE_USER.getColumnName();

	private static final String UPDATE_USER = DatabaseAuditMetaFieldEnum.UPDATE_USER.getColumnName();

	/**
	 * 使用mp做添加操作时候，这个方法执行
	 */
	@Override
	public void insertFill(MetaObject metaObject) {
		// 设置属性值
		this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
		this.setFieldValByName(CREATE_TIME, LocalDateTime.now(), metaObject);
		this.setFieldValByName(UPDATE_TIME, LocalDateTime.now(), metaObject);

		Long userId = SecurityUserUtils.getUserId();
		if (userId != null) {
			this.setFieldValByName(CREATE_USER, SecurityUserUtils.getUserId(), metaObject);
			this.setFieldValByName(UPDATE_USER, SecurityUserUtils.getUserId(), metaObject);
		}
	}

	/**
	 * 使用mp做修改操作时候，这个方法执行
	 */
	@Override
	public void updateFill(MetaObject metaObject) {
		Long userId = SecurityUserUtils.getUserId();

		if (userId != null) {
			this.setFieldValByName(UPDATE_TIME, LocalDateTime.now(), metaObject);
			this.setFieldValByName(UPDATE_USER, userId, metaObject);
		}
	}

}