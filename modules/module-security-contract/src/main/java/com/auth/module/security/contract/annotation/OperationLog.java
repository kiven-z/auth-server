package com.auth.module.security.contract.annotation;

import com.auth.module.security.contract.api.audit.OperationLogKind;

import java.lang.annotation.*;

/**
 * 标记需要写入操作日志的 Web 控制器方法（或类上默认，方法可覆盖）
 *
 * <p>
 * 可选的 SpEL 在方法执行结束后求值，上下文中包含方法参数名（若编译带 {@code -parameters}）以及变量 {@code result}（返回值，异常时为
 * {@code null}）
 * </p>
 *
 * @author Bunny
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

	/**
	 * 业务模块编码，写入 {@code log_operation.module}
	 * @return 模块编码，如 USER_ROLE
	 */
	String module();

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
	 * 操作对象 ID 的 SpEL，结果将 {@link String#valueOf} 或集合拼接后写入 {@code target_id}
	 * @return SpEL，空表示不记录
	 */
	String targetId() default "";

	/**
	 * 变更前快照的 SpEL，求值结果将序列化为 JSON 写入 {@code old_value}
	 * @return SpEL，空表示不记录
	 */
	String oldValue() default "";

	/**
	 * 变更后快照的 SpEL，求值结果将序列化为 JSON 写入 {@code new_value}
	 * @return SpEL，空表示不记录
	 */
	String newValue() default "";

	/**
	 * 是否记录查询参数与表单参数（脱敏由落库方策略控制，此处仅做长度截断）
	 * @return 默认记录
	 */
	boolean recordParams() default true;

	/**
	 * 是否尝试记录请求体（需由上游 {@code ContentCachingRequestWrapper} 等缓存 body，否则为空）
	 * @return 默认不记录
	 */
	boolean recordBody() default false;

	/**
	 * 当返回值为统一 {@code Result} 包装类型时是否写入其 {@code message} 至 {@code response_message}
	 * @return 默认写入
	 */
	boolean recordResultMessage() default true;

}
