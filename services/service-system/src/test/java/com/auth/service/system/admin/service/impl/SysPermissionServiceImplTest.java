package com.auth.service.system.admin.service.impl;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysPermissionMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.form.permission.SysPermissionCreateForm;
import com.auth.service.system.admin.model.vo.permission.SysPermissionDetailVO;
import com.auth.service.system.authorization.dispatch.trigger.PermissionAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link SysPermissionServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysPermissionServiceImpl 系统权限")
@ExtendWith(MockitoExtension.class)
class SysPermissionServiceImplTest {

	@Mock
	private SysPermissionMapper sysPermissionMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private PermissionAuthorizationInvalidationTrigger permissionInvalidationTrigger;

	private SysPermissionServiceImpl sysPermissionService;

	@BeforeEach
	void setUp() throws Exception {
		sysPermissionService = new SysPermissionServiceImpl(auditUserDisplayService, permissionInvalidationTrigger);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysPermissionService, sysPermissionMapper);
	}

	@Test
	@DisplayName("新增权限：编码已存在时抛出 PERMISSION_CODE_DUPLICATE")
	void createThrowsWhenPermissionCodeExists() {
		SysPermissionCreateForm form = new SysPermissionCreateForm();
		form.setPermissionCode("sys:user:list");
		form.setPermissionName("列表");
		form.setPermissionType(0);
		when(sysPermissionMapper.selectCount(any())).thenReturn(1L);

		assertThatThrownBy(() -> sysPermissionService.create(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.PERMISSION_CODE_DUPLICATE);
	}

	@Test
	@DisplayName("权限详情：不存在时抛出 PERMISSION_NOT_FOUND")
	void getDetailThrowsWhenNotFound() {
		when(sysPermissionMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysPermissionService.getDetail(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.PERMISSION_NOT_FOUND);
	}

	@Test
	@DisplayName("权限详情：返回 VO 并填充审计用户名")
	void getDetailReturnsVoAndEnrichesAuditUsernames() {
		SysPermissionEntity entity = new SysPermissionEntity();
		entity.setId(3L);
		entity.setPermissionCode("sys:dept:query");
		entity.setPermissionName("部门查询");
		entity.setPermissionType(1);
		entity.setStatus(true);
		when(sysPermissionMapper.selectById(3L)).thenReturn(entity);

		SysPermissionDetailVO detail = sysPermissionService.getDetail(3L);

		assertThat(detail.getPermissionCode()).isEqualTo("sys:dept:query");
		assertThat(detail.getPermissionType()).isEqualTo(1);
		verify(auditUserDisplayService).enrichAuditUsernames(any(List.class));
	}

	@Test
	@DisplayName("删除权限：不存在时抛出 PERMISSION_NOT_FOUND")
	void deleteThrowsWhenNotFound() {
		when(sysPermissionMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysPermissionService.deleteById(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.PERMISSION_NOT_FOUND);

		verify(sysPermissionMapper, never()).deleteById(any());
	}

	@Test
	@DisplayName("删除权限：物理删除并触发授权失效")
	void deleteShouldPhysicallyDeleteAndInvalidate() {
		SysPermissionEntity entity = new SysPermissionEntity();
		entity.setId(5L);
		entity.setPermissionCode("sys:role:delete");
		when(sysPermissionMapper.selectById(5L)).thenReturn(entity);

		sysPermissionService.deleteById(5L);

		verify(sysPermissionMapper).deleteById(5L);
		verify(permissionInvalidationTrigger).submitByPermissionCodes(List.of("sys:role:delete"),
				"delete:sys:role:delete");
	}

}
