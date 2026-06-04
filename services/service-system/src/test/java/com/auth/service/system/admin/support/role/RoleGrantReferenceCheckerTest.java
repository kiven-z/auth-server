package com.auth.service.system.admin.support.role;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.model.enums.RoleTypeEnum;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link RoleGrantReferenceChecker} 单元测试。
 */
@DisplayName("RoleGrantReferenceChecker grant 角色校验")
@ExtendWith(MockitoExtension.class)
class RoleGrantReferenceCheckerTest {

	@Mock
	private SysRoleMapper sysRoleMapper;

	@InjectMocks
	private RoleGrantReferenceChecker roleGrantReferenceChecker;

	@Test
	@DisplayName("空列表跳过校验")
	void requireDeptGrantEligibleRolesSkipsWhenEmpty() {
		assertThatCode(() -> roleGrantReferenceChecker.requireDeptGrantEligibleRoles(List.of()))
			.doesNotThrowAnyException();

		verify(sysRoleMapper, never()).selectCount(any());
	}

	@Test
	@DisplayName("全部 eligible 时不抛异常")
	void requireDeptGrantEligibleRolesPassesWhenAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(2L);

		assertThatCode(() -> roleGrantReferenceChecker.requireDeptGrantEligibleRoles(List.of("BIZ_ADMIN", "CUSTOM_A")))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("存在无效角色时抛出 DEPT_ROLE_INVALID")
	void requireDeptGrantEligibleRolesThrowsWhenNotAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(0L);

		List<String> roleCodes = List.of("SYS_ADMIN");
		assertThatThrownBy(() -> roleGrantReferenceChecker.requireDeptGrantEligibleRoles(roleCodes))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.DEPT_ROLE_INVALID);
	}

	@Test
	@DisplayName("requireGrantEligibleRoles 可指定 allowedRoleTypes 与结果码")
	void requireGrantEligibleRolesUsesCustomAllowedTypes() {
		when(sysRoleMapper.selectCount(any())).thenReturn(1L);

		assertThatCode(() -> roleGrantReferenceChecker.requireGrantEligibleRoles(List.of("MENU_ADMIN"),
				List.of(RoleTypeEnum.MENU.getCode()), SystemAdminResultCode.ROLE_NOT_FOUND))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("菜单关联：全部 eligible 时不抛异常")
	void requireMenuAssignEligibleRolesPassesWhenAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(1L);

		assertThatCode(() -> roleGrantReferenceChecker.requireMenuAssignEligibleRoles(List.of("MENU_ADMIN")))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("岗位 grant：全部 eligible 时不抛异常")
	void requirePostGrantEligibleRolesPassesWhenAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(1L);

		assertThatCode(() -> roleGrantReferenceChecker.requirePostGrantEligibleRoles(List.of("CUSTOM_A")))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("岗位 grant：存在无效角色时抛出 POST_ROLE_INVALID")
	void requirePostGrantEligibleRolesThrowsWhenNotAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(0L);

		List<String> roleCodes = List.of("SYS_ADMIN");
		assertThatThrownBy(() -> roleGrantReferenceChecker.requirePostGrantEligibleRoles(roleCodes))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.POST_ROLE_INVALID);
	}

	@Test
	@DisplayName("菜单关联：存在无效角色时抛出 MENU_ROLE_INVALID")
	void requireMenuAssignEligibleRolesThrowsWhenNotAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(0L);

		List<String> roleCodes = List.of("BIZ_ADMIN");
		assertThatThrownBy(() -> roleGrantReferenceChecker.requireMenuAssignEligibleRoles(roleCodes))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.MENU_ROLE_INVALID);
	}

}
