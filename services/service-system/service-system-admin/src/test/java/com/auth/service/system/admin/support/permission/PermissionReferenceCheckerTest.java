package com.auth.service.system.admin.support.permission;

import com.auth.service.system.admin.mapper.permission.SysPermissionMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PermissionReferenceChecker} 单元测试。
 */
@DisplayName("PermissionReferenceChecker 权限码校验")
@ExtendWith(MockitoExtension.class)
class PermissionReferenceCheckerTest {

	@Mock
	private SysPermissionMapper sysPermissionMapper;

	@InjectMocks
	private PermissionReferenceChecker permissionReferenceChecker;

	@Test
	@DisplayName("空列表跳过校验")
	void requireActivePermissionCodesSkipsWhenEmpty() {
		assertThatCode(() -> permissionReferenceChecker.requireActivePermissionCodes(List.of()))
			.doesNotThrowAnyException();

		verify(sysPermissionMapper, never()).selectList(any());
	}

	@Test
	@DisplayName("全部已启用时不抛异常")
	void requireActivePermissionCodesPassesWhenAllActive() {
		SysPermissionEntity listPerm = new SysPermissionEntity();
		listPerm.setId(1L);
		listPerm.setPermissionCode("sys:user:list");
		SysPermissionEntity queryPerm = new SysPermissionEntity();
		queryPerm.setId(2L);
		queryPerm.setPermissionCode("sys:user:query");
		when(sysPermissionMapper.selectList(any())).thenReturn(List.of(listPerm, queryPerm));

		assertThatCode(() -> permissionReferenceChecker
			.requireActivePermissionCodes(List.of("sys:user:list", "sys:user:query"))).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("存在无效或未启用权限码时抛出 GRANT_REFERENCE_INVALID")
	void requireActivePermissionCodesThrowsWhenNotAllActive() {
		when(sysPermissionMapper.selectList(any())).thenReturn(List.of());

		List<String> permissionCodes = List.of("missing:perm");
		assertThatThrownBy(() -> permissionReferenceChecker.requireActivePermissionCodes(permissionCodes))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
	}

	@Test
	@DisplayName("resolveActivePermissionIds：返回与入参顺序一致的权限 ID")
	void resolveActivePermissionIdsReturnsIdsInInputOrder() {
		SysPermissionEntity createPerm = new SysPermissionEntity();
		createPerm.setId(10L);
		createPerm.setPermissionCode("sys:user:create");
		SysPermissionEntity queryPerm = new SysPermissionEntity();
		queryPerm.setId(20L);
		queryPerm.setPermissionCode("sys:user:query");
		when(sysPermissionMapper.selectList(any())).thenReturn(List.of(createPerm, queryPerm));

		List<Long> permissionIds = permissionReferenceChecker
			.resolveActivePermissionIds(List.of("sys:user:create", "sys:user:query"));

		assertThat(permissionIds).containsExactly(10L, 20L);
	}

}
