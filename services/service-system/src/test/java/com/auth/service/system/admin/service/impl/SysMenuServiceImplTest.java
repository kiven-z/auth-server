package com.auth.service.system.admin.service.impl;

import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.admin.mapper.SysMenuMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuMoveForm;
import com.auth.service.system.admin.service.MenuWebRouteTreeBuilder;
import com.auth.service.system.admin.support.menu.MenuReferenceChecker;
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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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

	@Mock
	private MenuReferenceChecker menuReferenceChecker;

	private SysMenuServiceImpl sysMenuService;

	@BeforeEach
	void setUp() throws Exception {
		sysMenuService = new SysMenuServiceImpl(auditUserDisplayService, authInnerFeignClient, menuWebRouteTreeBuilder,
				menuReferenceChecker);
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
		List<Long> menuIds = List.of(2L);

		assertThatThrownBy(() -> sysMenuService.deleteByIds(menuIds)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.TREE_HAS_ACTIVE_CHILDREN);

		verify(sysMenuMapper, never()).deleteByIds(any());
	}

	@Test
	@DisplayName("移动菜单：父级未变时跳过更新")
	void moveSkipsUpdateWhenParentUnchanged() {
		SysMenuEntity existing = menuEntity(6L, 0L);
		when(menuReferenceChecker.getExisting(6L)).thenReturn(existing);
		when(menuReferenceChecker.normalizeAndRequireParent(0L)).thenReturn(0L);

		SysMenuMoveForm form = new SysMenuMoveForm();
		form.setParentId(0L);
		sysMenuService.move(6L, form);

		verify(sysMenuMapper, never()).updateById(any(SysMenuEntity.class));
	}

	@Test
	@DisplayName("移动菜单：父级变更时更新 parentId")
	void moveUpdatesParentWhenChanged() {
		SysMenuEntity existing = menuEntity(7L, 0L);
		when(menuReferenceChecker.getExisting(7L)).thenReturn(existing);
		when(menuReferenceChecker.normalizeAndRequireParent(12L)).thenReturn(12L);
		when(sysMenuMapper.updateById(any(SysMenuEntity.class))).thenReturn(1);

		SysMenuMoveForm form = new SysMenuMoveForm();
		form.setParentId(12L);
		sysMenuService.move(7L, form);

		verify(menuReferenceChecker).requireMoveTargetValid(7L, 12L);
		verify(sysMenuMapper).updateById(argThat((SysMenuEntity e) -> Long.valueOf(12L).equals(e.getParentId())));
	}

	@Test
	@DisplayName("删除菜单：无子节点冲突时批量物理删除")
	void deleteShouldBatchRemoveByIds() {
		when(sysMenuMapper.selectFirstBlockedParentId(List.of(3L, 3L, 4L))).thenReturn(null);

		sysMenuService.deleteByIds(Arrays.asList(3L, 3L, null, 4L));

		verify(sysMenuMapper).selectFirstBlockedParentId(List.of(3L, 3L, 4L));
		verify(sysMenuMapper).deleteByIds(anyCollection());
	}

	private static SysMenuEntity menuEntity(Long id, Long parentId) {
		SysMenuEntity entity = new SysMenuEntity();
		entity.setId(id);
		entity.setParentId(parentId);
		return entity;
	}

}
