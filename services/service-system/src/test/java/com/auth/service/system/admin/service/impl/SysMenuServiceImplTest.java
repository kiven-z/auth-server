package com.auth.service.system.admin.service.impl;

import com.auth.service.system.admin.mapper.SysMenuMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.service.MenuWebRouteTreeBuilder;
import com.auth.service.system.common.feign.AuthInnerFeignClient;
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

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * {@link SysMenuServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysMenuServiceImpl 系统菜单")
@ExtendWith(MockitoExtension.class)
class SysMenuServiceImplTest {

	@Mock
	private SysMenuMapper sysMenuMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private AuthInnerFeignClient authInnerFeignClient;

	@Mock
	private MenuWebRouteTreeBuilder menuWebRouteTreeBuilder;

	private SysMenuServiceImpl sysMenuService;

	@BeforeEach
	void setUp() throws Exception {
		sysMenuService = new SysMenuServiceImpl(auditUserDisplayService, authInnerFeignClient, menuWebRouteTreeBuilder);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysMenuService, sysMenuMapper);
	}

	@Test
	@DisplayName("删除菜单：空列表直接返回")
	void deleteIgnoresEmptyIds() {
		sysMenuService.deleteByIds(List.of());

		verifyNoInteractions(sysMenuMapper);
	}

	@Test
	@DisplayName("删除菜单：批量物理删除主表（关联由外键 CASCADE 维护）")
	void deleteShouldBatchRemoveByIds() {
		sysMenuService.deleteByIds(List.of(3L, 3L, null, 4L));

		verify(sysMenuMapper).deleteByIds(anyCollection());
	}

}
