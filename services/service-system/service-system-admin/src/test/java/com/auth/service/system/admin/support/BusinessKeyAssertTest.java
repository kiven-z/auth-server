package com.auth.service.system.admin.support;

import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.support.BusinessKeyAssert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
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
	private IService<SysRoleEntity> roleService;

	@Test
	@DisplayName("requireAbsent：无冲突记录时不抛异常")
	void requireAbsentPassesWhenCountZero() {
		when(roleService.count(any())).thenReturn(0L);

		BusinessKeyAssert.requireAbsent(roleService,
				Wrappers.<SysRoleEntity>lambdaQuery().eq(SysRoleEntity::getRoleCode, "ADMIN"),
				SystemCommonResultCode.DATA_CODE_DUPLICATE, "ADMIN");

		verify(roleService).count(any());
	}

	@Test
	@DisplayName("requireAbsent：存在冲突记录时抛出 DUPLICATE 业务异常")
	void requireAbsentThrowsWhenCountPositive() {
		when(roleService.count(any())).thenReturn(1L);
		var duplicateQuery = Wrappers.<SysRoleEntity>lambdaQuery().eq(SysRoleEntity::getRoleCode, "ADMIN");

		assertThatThrownBy(() -> BusinessKeyAssert.requireAbsent(roleService, duplicateQuery,
				SystemCommonResultCode.DATA_CODE_DUPLICATE, "ADMIN"))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_CODE_DUPLICATE);
	}

}
