package com.auth.service.system.admin.service.impl;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleCreateForm;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysRoleServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysRoleServiceImpl 系统角色")
@ExtendWith(MockitoExtension.class)
class SysRoleServiceImplTest {

	@Mock
	private SysRoleMapper sysRoleMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private SysRoleServiceImpl sysRoleService;

	/**
	 * 最小合法新增表单
	 */
	private static SysRoleCreateForm minimalForm() {
		SysRoleCreateForm form = new SysRoleCreateForm();
		form.setRoleCode("CUSTOM_R_ONE");
		form.setRoleName("自定义角色");
		form.setStatus(true);
		form.setRoleType(4);
		return form;
	}

	@BeforeEach
	void setUp() throws Exception {
		sysRoleService = new SysRoleServiceImpl(auditUserDisplayService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysRoleService, sysRoleMapper);
	}

	@Test
	@DisplayName("新增角色：角色编码已存在时抛出 ROLE_CODE_DUPLICATE")
	void createThrowsWhenRoleCodeExists() {
		SysRoleCreateForm form = minimalForm();
		when(sysRoleMapper.selectCount(any())).thenReturn(1L);

		assertThatThrownBy(() -> sysRoleService.create(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.ROLE_CODE_DUPLICATE);
	}

	@Test
	@DisplayName("新增角色：编码去空格、排序号默认 0 并持久化")
	void createTrimsRoleCodeAppliesDefaultsAndPersists() {
		SysRoleCreateForm form = minimalForm();
		form.setRoleCode("  CUSTOM_R_ONE  ");
		form.setOrderNum(null);
		when(sysRoleMapper.selectCount(any())).thenReturn(0L);
		when(sysRoleMapper.insert(any(SysRoleEntity.class))).thenReturn(1);

		sysRoleService.create(form);

		verify(sysRoleMapper).insert(argThat((SysRoleEntity e) -> "CUSTOM_R_ONE".equals(e.getRoleCode())
				&& Integer.valueOf(0).equals(e.getOrderNum()) && Boolean.TRUE.equals(e.getStatus())));
	}

}
