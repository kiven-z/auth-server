package com.auth.module.security.contract.annotation;

import com.auth.module.security.contract.api.audit.AuditBizModule;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;

import java.lang.annotation.*;

/**
 * 标记需要写入操作日志的 Web 控制器方法（或类上默认，方法可覆盖）。
 *
 * @author Bunny
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

	/**
	 * 大模块：与微服务（如 system、auth、example）对齐。
	 * @return 审计服务域
	 */
	AuditServiceDomain serviceDomain();

	/**
	 * 小模块：本服务内业务域（如 {@code SYS_DEPT}、{@code SYS_MENU}），枚举名与落库小模块段一致。
	 * @return 业务模块枚举
	 */
	AuditBizModule bizModule();

	/**
	 * 操作类型
	 * @return 操作类型
	 */
	OperationLogKind operation();

	/**
	 * 操作对象类型，如 User、Dept
	 * @return 类型文案，可为空
	 */
	String targetType() default "";

	/**
	 * 是否记录查询参数与表单参数（脱敏由落库方策略控制，此处仅做长度截断）
	 * @return 默认记录
	 */
	boolean recordParams() default true;

	/**
	 * 当返回值为统一 {@code Result} 包装类型时是否写入其 {@code message} 至 {@code response_message}
	 * @return 默认写入
	 */
	boolean recordResultMessage() default true;

}
