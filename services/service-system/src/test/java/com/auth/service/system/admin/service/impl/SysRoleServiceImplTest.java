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
import com.auth.service.system.common.exception.SystemResultCodeEnum;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * {@link SysRoleServiceImpl} 单元测试（分页补全用户名委托 {@link AuditUserDisplayService}）。
 *
 * @author Bunny
 */
@DisplayName("SysRoleServiceImpl 角色管理")
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
	@DisplayName("分页查询：Mapper 分页后调用审计用户展示名补全")
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
	@DisplayName("详情：角色不存在时抛出 ROLE_NOT_FOUND")
	void getDetailThrowsWhenRoleMissing() {
		when(sysRoleMapper.selectById(404L)).thenReturn(null);

		assertThatThrownBy(() -> sysRoleService.getDetail(404L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemResultCodeEnum.ROLE_NOT_FOUND);
	}

	@Test
	@DisplayName("详情：复制实体并填充 roleTypeDesc")
	void getDetailReturnsVoWithRoleTypeDesc() {
		SysRoleEntity entity = new SysRoleEntity();
		entity.setId(1L);
		entity.setRoleCode("ADMIN");
		entity.setRoleName("管理员");
		entity.setStatus(true);
		entity.setRoleType(1);
		entity.setOrderNum(2);
		when(sysRoleMapper.selectById(1L)).thenReturn(entity);

		SysRoleDetailVO vo = sysRoleService.getDetail(1L);

		assertThat(vo.getRoleCode()).isEqualTo("ADMIN");
		assertThat(vo.getRoleName()).isEqualTo("管理员");
		assertThat(vo.getRoleTypeDesc()).isEqualTo("系统角色");
	}

	@Test
	@DisplayName("新增：持久化由 save → insert 完成")
	void createPersistsViaInsert() {
		SysRoleCreateForm form = new SysRoleCreateForm();
		form.setRoleCode("CUSTOM_R_ONE");
		form.setRoleName("自定义");
		form.setStatus(true);
		form.setRoleType(4);
		form.setOrderNum(0);
		when(sysRoleMapper.insert(org.mockito.ArgumentMatchers.<SysRoleEntity>any())).thenReturn(1);

		sysRoleService.create(form);

		verify(sysRoleMapper).insert(argThat((SysRoleEntity e) -> "CUSTOM_R_ONE".equals(e.getRoleCode())
				&& Integer.valueOf(4).equals(e.getRoleType())));
	}

	@Test
	@DisplayName("下拉选项：委托 Mapper 查询并映射为 VO")
	void listOptionsDelegatesToMapper() {
		SysRoleEntity entity = new SysRoleEntity();
		entity.setId(1L);
		entity.setRoleCode("R1");
		entity.setRoleName("角色1");
		when(sysRoleMapper.selectRoleOptions("a", "b")).thenReturn(List.of(entity));

		List<SysRoleOptionVO> list = sysRoleService.listOptions("a", "b");

		assertThat(list).hasSize(1);
		assertThat(list.get(0).getId()).isEqualTo(1L);
		assertThat(list.get(0).getRoleCode()).isEqualTo("R1");
		verify(sysRoleMapper).selectRoleOptions("a", "b");
	}

}
