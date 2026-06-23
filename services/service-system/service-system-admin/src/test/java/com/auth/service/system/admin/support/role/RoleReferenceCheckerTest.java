package com.auth.service.system.admin.support.role;

import com.auth.service.system.admin.mapper.role.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * {@link RoleReferenceChecker} 单元测试。
 */
@DisplayName("RoleReferenceChecker 角色 ID 校验")
@ExtendWith(MockitoExtension.class)
class RoleReferenceCheckerTest {

	@Mock
	private SysRoleMapper sysRoleMapper;

	@InjectMocks
	private RoleReferenceChecker roleReferenceChecker;

	@Test
	@DisplayName("角色 ID 为空时抛出 DATA_NOT_EXIST")
	void getExistingByRoleIdThrowsWhenNull() {
		assertThatThrownBy(() -> roleReferenceChecker.getExistingByRoleId(null))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("角色不存在时抛出 DATA_NOT_EXIST")
	void getExistingByRoleIdThrowsWhenMissing() {
		when(sysRoleMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(() -> roleReferenceChecker.getExistingByRoleId(99L))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("角色存在时返回实体")
	void getExistingByRoleIdReturnsEntity() {
		SysRoleEntity role = new SysRoleEntity();
		role.setId(1L);
		role.setRoleCode("ADMIN");
		when(sysRoleMapper.selectById(1L)).thenReturn(role);

		SysRoleEntity result = roleReferenceChecker.getExistingByRoleId(1L);

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getRoleCode()).isEqualTo("ADMIN");
	}

}
