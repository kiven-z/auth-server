package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysPermissionMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.form.permission.SysPermissionCreateForm;
import com.auth.service.system.admin.model.query.SysPermissionQuery;
import com.auth.service.system.admin.model.vo.permission.SysPermissionPageResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysPermissionServiceImpl} ????
 *
 * @author Bunny
 */
@DisplayName("SysPermissionServiceImpl ????")
@ExtendWith(MockitoExtension.class)
class SysPermissionServiceImplTest {

	@Mock
	private SysPermissionMapper sysPermissionMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private SysPermissionServiceImpl sysPermissionService;

	private static SysPermissionCreateForm buildValidForm() {
		SysPermissionCreateForm form = new SysPermissionCreateForm();
		form.setPermissionCode("sys:user:add");
		form.setPermissionName("????");
		form.setPermissionType(1);
		form.setRemark("r");
		return form;
	}

	@BeforeEach
	void setUp() throws Exception {
		sysPermissionService = new SysPermissionServiceImpl(auditUserDisplayService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysPermissionService, sysPermissionMapper);
	}

	@Test
	@DisplayName("?????Mapper ??????????????")
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
	@DisplayName("????????????? PERMISSION_CODE_DUPLICATE")
	void createThrowsWhenPermissionCodeExists() {
		SysPermissionCreateForm form = buildValidForm();
		when(sysPermissionMapper.selectCount(any())).thenReturn(1L);

		assertThatThrownBy(() -> sysPermissionService.create(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.PERMISSION_CODE_DUPLICATE);
	}

	@Test
	@DisplayName("??????????????????? Web ? strip ????")
	void createAppliesDefaultsAndPersists() {
		SysPermissionCreateForm form = buildValidForm();
		form.setPermissionCode("sys:user:add");
		form.setStatus(null);
		form.setOrderNum(null);
		when(sysPermissionMapper.selectCount(any())).thenReturn(0L);
		when(sysPermissionMapper.insert(any(SysPermissionEntity.class))).thenReturn(1);

		sysPermissionService.create(form);

		verify(sysPermissionMapper)
			.insert(argThat((SysPermissionEntity e) -> "sys:user:add".equals(e.getPermissionCode())
					&& Boolean.TRUE.equals(e.getStatus()) && Integer.valueOf(0).equals(e.getOrderNum())));
	}

}
