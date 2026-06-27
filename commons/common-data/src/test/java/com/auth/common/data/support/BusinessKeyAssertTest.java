package com.auth.common.data.support;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link BusinessKeyAssert} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("BusinessKeyAssert 业务键唯一性断言")
@ExtendWith(MockitoExtension.class)
class BusinessKeyAssertTest {

	@Mock
	private BaseMapper<TestEntity> entityMapper;

	@Test
	@DisplayName("requireAbsent：无冲突记录时不抛异常")
	void requireAbsentPassesWhenCountZero() {
		when(entityMapper.selectCount(any())).thenReturn(0L);

		BusinessKeyAssert.requireAbsent(entityMapper,
				Wrappers.<TestEntity>lambdaQuery().eq(TestEntity::getCode, "ADMIN"),
				() -> new IllegalStateException("duplicate"));

		verify(entityMapper).selectCount(any());
	}

	@Test
	@DisplayName("requireAbsent：存在冲突记录时抛出调用方提供的异常")
	void requireAbsentThrowsWhenCountPositive() {
		when(entityMapper.selectCount(any())).thenReturn(1L);
		var duplicateQuery = Wrappers.<TestEntity>lambdaQuery().eq(TestEntity::getCode, "ADMIN");

		assertThatThrownBy(() -> BusinessKeyAssert.requireAbsent(entityMapper, duplicateQuery,
				() -> new IllegalStateException("duplicate")))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("duplicate");
	}

	private static class TestEntity {

		private String code;

		String getCode() {
			return code;
		}

	}

}
