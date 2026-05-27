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
	 * 唯一性冲突时抛出业务异常
	 */
	public static <T> void requireAbsent(IService<T> service, Wrapper<T> uniquenessWrapper,
			SystemResultCode duplicateCode, Object... args) {
		if (service.count(uniquenessWrapper) > 0) {
			throw new SystemBusinessException(duplicateCode, args);
		}
	}

}
