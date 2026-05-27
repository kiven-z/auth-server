package com.auth.service.system.common.support;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemResultCode;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.experimental.UtilityClass;

/**
 * 业务键（编码等）在库内唯一性断言
 *
 * @author Bunny
 */
@UtilityClass
public class BusinessKeyAssert {

	/**
	 * 要求 uniquenessWrapper 匹配不到任何记录；否则抛出 duplicateCode 业务异常
	 * @param service MyBatis-Plus 服务（通常为 {@code this}）
	 * @param uniquenessWrapper 唯一性查询条件
	 * @param duplicateCode 冲突时的结果码
	 * @param args 异常占位参数（传入 i18n）
	 * @param <T> 实体类型
	 */
	public static <T> void requireAbsent(IService<T> service, Wrapper<T> uniquenessWrapper,
			SystemResultCode duplicateCode, Object... args) {
		if (service.count(uniquenessWrapper) > 0) {
			throw new SystemBusinessException(duplicateCode, args);
		}
	}

}
