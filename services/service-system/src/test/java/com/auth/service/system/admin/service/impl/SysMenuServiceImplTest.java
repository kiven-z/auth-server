package com.auth.service.system.admin.service.impl;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysMenuMapper;
import com.auth.service.system.admin.service.MenuWebRouteTreeBuilder;
import com.auth.service.system.common.exception.SystemBusinessException;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
	@DisplayName("删除菜单：存在未一并删除的子菜单时抛出 MENU_HAS_CHILDREN")
	void deleteThrowsWhenHasUndeletedChildren() {
		when(sysMenuMapper.selectFirstBlockedParentId(List.of(2L))).thenReturn(2L);

		assertThatThrownBy(() -> sysMenuService.deleteByIds(List.of(2L))).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.MENU_HAS_CHILDREN);

		verify(sysMenuMapper, never()).deleteByIds(any());
	}

	@Test
	@DisplayName("删除菜单：无子节点冲突时批量物理删除")
	void deleteShouldBatchRemoveByIds() {
		when(sysMenuMapper.selectFirstBlockedParentId(List.of(3L, 4L))).thenReturn(null);

		sysMenuService.deleteByIds(List.of(3L, 3L, null, 4L));

		verify(sysMenuMapper).deleteByIds(anyCollection());
	}

}
