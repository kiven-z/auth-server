package com.auth.service.system.admin.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleCreateForm;
import com.auth.service.system.admin.model.query.SysRoleQuery;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import com.auth.service.system.admin.model.vo.role.SysRoleOptionVO;
import com.auth.service.system.admin.model.vo.role.SysRolePageResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.admin.SystemAdminResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * {@link SysRoleServiceImpl} ?????????????? {@link AuditUserDisplayService}??
 *
 * @author Bunny
 */
@DisplayName("SysRoleServiceImpl ????")
@ExtendWith(MockitoExtension.class)
class SysRoleServiceImplTest {

	@Mock
	private SysRoleMapper sysRoleMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private SysRoleServiceImpl sysRoleService;

	@BeforeEach
	void setUp() throws Exception {
		sysRoleService = new SysRoleServiceImpl(auditUserDisplayService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysRoleService, sysRoleMapper);
	}

	@Test
	@DisplayName("?????Mapper ??????????????")
	void getPageDelegatesEnrichToAuditUserDisplayService() {
		Page<SysRoleEntity> pageParams = new Page<>(1, 10);
		SysRoleQuery query = new SysRoleQuery();
		Page<SysRoleEntity> mpPage = new Page<>(1, 10);
		SysRoleEntity entityRow = new SysRoleEntity();
		entityRow.setId(1L);
		mpPage.setRecords(List.of(entityRow));
		when(sysRoleMapper.selectListByPage(eq(pageParams), eq(query), anyString())).thenReturn(mpPage);

		PageResponse<SysRolePageResponse> result = sysRoleService.getPage(pageParams, query);

		assertThat(result.getList()).hasSize(1);
		verify(auditUserDisplayService)
			.enrichAuditUsernames(org.mockito.ArgumentMatchers.<IPage<SysRolePageResponse>>any());
	}

	@Test
	@DisplayName("??????????? ROLE_NOT_FOUND")
	void getDetailThrowsWhenRoleMissing() {
		when(sysRoleMapper.selectById(404L)).thenReturn(null);

		assertThatThrownBy(() -> sysRoleService.getDetail(404L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.ROLE_NOT_FOUND);
	}

	@Test
	@DisplayName("?????????? roleTypeDesc")
	void getDetailReturnsVoWithRoleTypeDesc() {
		SysRoleEntity entity = new SysRoleEntity();
		entity.setId(1L);
		entity.setRoleCode("ADMIN");
		entity.setRoleName("\u7ba1\u7406\u5458");
		entity.setStatus(true);
		entity.setRoleType(1);
		entity.setOrderNum(2);
		when(sysRoleMapper.selectById(1L)).thenReturn(entity);

		SysRoleDetailVO vo = sysRoleService.getDetail(1L);

		assertThat(vo.getRoleCode()).isEqualTo("ADMIN");
		assertThat(vo.getRoleName()).isEqualTo("\u7ba1\u7406\u5458");
		assertThat(vo.getRoleTypeDesc()).isEqualTo("\u7cfb\u7edf\u89d2\u8272");
	}

	@Test
	@DisplayName("??????? save ? insert ??")
	void createPersistsViaInsert() {
		SysRoleCreateForm form = new SysRoleCreateForm();
		form.setRoleCode("CUSTOM_R_ONE");
		form.setRoleName("???");
		form.setStatus(true);
		form.setRoleType(4);
		form.setOrderNum(0);
		when(sysRoleMapper.insert(org.mockito.ArgumentMatchers.<SysRoleEntity>any())).thenReturn(1);

		sysRoleService.create(form);

		verify(sysRoleMapper).insert(argThat((SysRoleEntity e) -> "CUSTOM_R_ONE".equals(e.getRoleCode())
				&& Integer.valueOf(4).equals(e.getRoleType())));
	}

	@Test
	@DisplayName("??????? Mapper ?????? VO")
	void listOptionsDelegatesToMapper() {
		SysRoleEntity entity = new SysRoleEntity();
		entity.setId(1L);
		entity.setRoleCode("R1");
		entity.setRoleName("??1");
		when(sysRoleMapper.selectRoleOptions("a", "b")).thenReturn(List.of(entity));

		List<SysRoleOptionVO> list = sysRoleService.listOptions("a", "b");

		assertThat(list).hasSize(1);
		assertThat(list.get(0).getId()).isEqualTo(1L);
		assertThat(list.get(0).getRoleCode()).isEqualTo("R1");
		verify(sysRoleMapper).selectRoleOptions("a", "b");
	}

}
