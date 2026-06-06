package com.auth.service.system.admin.support.role;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
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
	void requireGrantTableOrgRolesSkipsWhenEmpty() {
		assertThatCode(() -> roleGrantReferenceChecker.requireGrantTableOrgRoles(GrantTableSubjectType.DEPT, List.of()))
			.doesNotThrowAnyException();

		verify(sysRoleMapper, never()).selectCount(any());
	}

	@Test
	@DisplayName("部门主体：全部 eligible 时不抛异常")
	void requireGrantTableOrgRolesPassesForDeptWhenAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(2L);

		assertThatCode(() -> roleGrantReferenceChecker.requireGrantTableOrgRoles(GrantTableSubjectType.DEPT,
				List.of("BIZ_ADMIN", "CUSTOM_A")))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("部门主体：存在无效角色时抛出 DEPT_ROLE_INVALID")
	void requireGrantTableOrgRolesThrowsDeptInvalidWhenNotAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(0L);

		List<String> roleCodes = List.of("SYS_ADMIN");
		assertThatThrownBy(
				() -> roleGrantReferenceChecker.requireGrantTableOrgRoles(GrantTableSubjectType.DEPT, roleCodes))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
	}

	@Test
	@DisplayName("requireGrantEligibleRoles 可指定 allowedRoleTypes 与结果码")
	void requireGrantEligibleRolesUsesCustomAllowedTypes() {
		when(sysRoleMapper.selectCount(any())).thenReturn(1L);

		assertThatCode(() -> roleGrantReferenceChecker.requireGrantEligibleRoles(List.of("MENU_ADMIN"),
				List.of(RoleTypeEnum.MENU.getCode()), SystemCommonResultCode.DATA_NOT_EXIST))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("菜单关联：全部 eligible 时不抛异常")
	void requireGrantEligibleRolesPassesForMenuAssignWhenAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(1L);

		assertThatCode(() -> roleGrantReferenceChecker.requireGrantEligibleRoles(List.of("MENU_ADMIN"),
				RoleTypeEnum.MENU_ASSIGN_TYPES, SystemCommonResultCode.GRANT_REFERENCE_INVALID))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("岗位主体：全部 eligible 时不抛异常")
	void requireGrantTableOrgRolesPassesForPostWhenAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(1L);

		assertThatCode(() -> roleGrantReferenceChecker.requireGrantTableOrgRoles(GrantTableSubjectType.POST,
				List.of("CUSTOM_A")))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("岗位主体：存在无效角色时抛出 POST_ROLE_INVALID")
	void requireGrantTableOrgRolesThrowsPostInvalidWhenNotAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(0L);

		List<String> roleCodes = List.of("SYS_ADMIN");
		assertThatThrownBy(
				() -> roleGrantReferenceChecker.requireGrantTableOrgRoles(GrantTableSubjectType.POST, roleCodes))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
	}

	@Test
	@DisplayName("菜单关联：存在无效角色时抛出 GRANT_REFERENCE_INVALID")
	void requireGrantEligibleRolesThrowsForMenuAssignWhenNotAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(0L);

		List<String> roleCodes = List.of("BIZ_ADMIN");
		assertThatThrownBy(() -> roleGrantReferenceChecker.requireGrantEligibleRoles(roleCodes,
				RoleTypeEnum.MENU_ASSIGN_TYPES, SystemCommonResultCode.GRANT_REFERENCE_INVALID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
	}

}
