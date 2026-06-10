package com.auth.service.system.admin.support.role;

import com.auth.service.system.admin.mapper.SysRoleMapper;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link RoleReferenceChecker} 单元测试。
 */
@DisplayName("RoleReferenceChecker 角色编码校验")
@ExtendWith(MockitoExtension.class)
class RoleReferenceCheckerTest {

	@Mock
	private SysRoleMapper sysRoleMapper;

	@InjectMocks
	private RoleReferenceChecker roleReferenceChecker;

	@Test
	@DisplayName("角色编码为空时抛出 DATA_NOT_EXIST")
	void getExistingByRoleCodeThrowsWhenBlank() {
		assertThatThrownBy(() -> roleReferenceChecker.getExistingByRoleCode(" "))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("角色不存在时抛出 DATA_NOT_EXIST")
	void getExistingByRoleCodeThrowsWhenMissing() {
		when(sysRoleMapper.selectOne(any())).thenReturn(null);

		assertThatThrownBy(() -> roleReferenceChecker.getExistingByRoleCode("MISSING"))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("角色存在时返回实体")
	void getExistingByRoleCodeReturnsEntity() {
		SysRoleEntity role = new SysRoleEntity();
		role.setRoleCode("ADMIN");
		when(sysRoleMapper.selectOne(any())).thenReturn(role);

		SysRoleEntity result = roleReferenceChecker.getExistingByRoleCode("ADMIN");

		assertThat(result.getRoleCode()).isEqualTo("ADMIN");
	}

}
