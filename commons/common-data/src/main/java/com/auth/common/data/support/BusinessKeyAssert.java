package com.auth.common.data.support;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

/**
 * 业务键（编码等）在库内唯一性断言
 *
 * @author Bunny
 */
@UtilityClass
public class BusinessKeyAssert {

	/**
	 * 唯一性冲突时由调用方提供的异常工厂抛出运行时异常
	 * @param mapper 数据访问 Mapper
	 * @param uniquenessWrapper 唯一性查询条件
	 * @param onDuplicate 存在冲突记录时的异常供应器
	 * @param <T> 实体类型
	 */
	public static <T> void requireAbsent(BaseMapper<T> mapper, Wrapper<T> uniquenessWrapper,
			Supplier<? extends RuntimeException> onDuplicate) {
		if (mapper.selectCount(uniquenessWrapper) > 0) {
			throw onDuplicate.get();
		}
	}

}
