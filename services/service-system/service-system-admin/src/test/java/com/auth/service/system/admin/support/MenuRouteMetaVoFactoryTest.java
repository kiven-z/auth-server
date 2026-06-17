package com.auth.service.system.admin.support;

import com.auth.service.system.admin.convert.MenuConvertContext;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.vo.menu.RouteMetaVO;
import com.auth.service.system.admin.support.menu.MenuRouteMetaVoFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MenuRouteMetaVoFactory} 映射校验
 */
class MenuRouteMetaVoFactoryTest {

	@Test
	@DisplayName("从菜单与上下文组装 meta，含 rank、roles、auths")
	void fromMenuEntity_mapsFieldsAndContext() {
		SysMenuEntity menu = new SysMenuEntity();
		menu.setId(10L);
		menu.setTitle("系统管理");
		menu.setIcon("ri:settings-3-line");
		menu.setMenuRank(99);
		menu.setTransitionName("fade");

		MenuConvertContext context = new MenuConvertContext(Map.of(10L, List.of("admin")),
				Map.of(10L, List.of("system:user:list")));

		RouteMetaVO meta = MenuRouteMetaVoFactory.fromMenuEntity(menu, context);

		assertThat(meta.getTitle()).isEqualTo("系统管理");
		assertThat(meta.getRank()).isEqualTo(99);
		assertThat(meta.getRoles()).containsExactly("admin");
		assertThat(meta.getAuths()).containsExactly("system:user:list");
		assertThat(meta.getTransition()).isNotNull();
		assertThat(meta.getTransition().getName()).contains("fade");
	}

	@Test
	@DisplayName("菜单 ID 无上下文条目时使用空列表")
	void fromMenuEntity_usesEmptyListsWhenContextMissing() {
		SysMenuEntity menu = new SysMenuEntity();
		menu.setId(1L);
		MenuConvertContext context = new MenuConvertContext(Map.of(), Map.of());

		RouteMetaVO meta = MenuRouteMetaVoFactory.fromMenuEntity(menu, context);

		assertThat(meta.getRoles()).isEmpty();
		assertThat(meta.getAuths()).isEmpty();
	}

}
