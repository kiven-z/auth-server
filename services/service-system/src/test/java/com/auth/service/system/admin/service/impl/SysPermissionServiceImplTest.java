package com.auth.service.system.admin.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.mapper.SysPermissionMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.form.permission.SysPermissionCreateForm;
import com.auth.service.system.admin.model.query.SysPermissionQuery;
import com.auth.service.system.admin.model.vo.permission.SysPermissionPageResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.SystemResultCodeEnum;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * {@link SysPermissionServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysPermissionServiceImpl 权限管理")
@ExtendWith(MockitoExtension.class)
class SysPermissionServiceImplTest {

	@Mock
	private SysPermissionMapper sysPermissionMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private SysPermissionServiceImpl sysPermissionService;

	@BeforeEach
	void setUp() throws Exception {
		sysPermissionService = new SysPermissionServiceImpl(auditUserDisplayService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysPermissionService, sysPermissionMapper);
	}

	@Test
	@DisplayName("分页查询：Mapper 分页后调用审计用户展示名补全")
	void getPageDelegatesEnrichToAuditUserDisplayService() {
		Page<SysPermissionEntity> pageParams = new Page<>(1, 10);
		SysPermissionQuery query = new SysPermissionQuery();
		Page<SysPermissionEntity> mpPage = new Page<>(1, 10);
		SysPermissionEntity entityRow = new SysPermissionEntity();
		entityRow.setId(1L);
		mpPage.setRecords(java.util.List.of(entityRow));
		when(sysPermissionMapper.selectListByPage(eq(pageParams), eq(query), anyString())).thenReturn(mpPage);

		PageResponse<SysPermissionPageResponse> result = sysPermissionService.getPage(pageParams, query);

		assertThat(result.getList()).hasSize(1);
		verify(auditUserDisplayService)
			.enrichAuditUsernames(org.mockito.ArgumentMatchers.<IPage<SysPermissionPageResponse>>any());
	}

	@Test
	@DisplayName("新增：权限编码格式非法时抛出 PERMISSION_CODE_FORMAT_INVALID")
	void createThrowsWhenPermissionCodeMalformed() {
		SysPermissionCreateForm form = buildValidForm();
		form.setPermissionCode("Sys:user:add");

		assertThatThrownBy(() -> sysPermissionService.create(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemResultCodeEnum.PERMISSION_CODE_FORMAT_INVALID);
		verify(sysPermissionMapper, never()).insert(org.mockito.ArgumentMatchers.<SysPermissionEntity>any());
	}

	@Test
	@DisplayName("新增：权限编码已存在时抛出 PERMISSION_CODE_DUPLICATE")
	void createThrowsWhenPermissionCodeExists() {
		SysPermissionCreateForm form = buildValidForm();
		when(sysPermissionMapper.selectCount(any())).thenReturn(1L);

		assertThatThrownBy(() -> sysPermissionService.create(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemResultCodeEnum.PERMISSION_CODE_DUPLICATE);
	}

	@Test
	@DisplayName("新增：默认启用与顺序，trim 编码后持久化")
	void createAppliesDefaultsAndPersists() {
		SysPermissionCreateForm form = buildValidForm();
		form.setPermissionCode("  sys:user:add  ");
		form.setStatus(null);
		form.setOrderNum(null);
		when(sysPermissionMapper.selectCount(any())).thenReturn(0L);
		when(sysPermissionMapper.insert(any(SysPermissionEntity.class))).thenReturn(1);

		sysPermissionService.create(form);

		verify(sysPermissionMapper)
			.insert(argThat((SysPermissionEntity e) -> "sys:user:add".equals(e.getPermissionCode())
					&& Boolean.TRUE.equals(e.getStatus()) && Integer.valueOf(0).equals(e.getOrderNum())));
	}

	private static SysPermissionCreateForm buildValidForm() {
		SysPermissionCreateForm form = new SysPermissionCreateForm();
		form.setPermissionCode("sys:user:add");
		form.setPermissionName("新增用户");
		form.setPermissionType(1);
		form.setRemark("r");
		return form;
	}

}
