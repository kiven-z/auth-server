package com.auth.service.system.admin.support;

import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SysMenuListVoConverter} 单元测试
 */
class SysMenuListVoConverterTest {

	@Test
	@DisplayName("启用状态映射为 API 0，停用映射为 1")
	void statusToApi_matchesFrontendContract() {
		assertThat(SysMenuListVoConverter.statusToApi(true)).isZero();
		assertThat(SysMenuListVoConverter.statusToApi(false)).isEqualTo(1);
		assertThat(SysMenuListVoConverter.statusToApi(null)).isEqualTo(1);
	}

	@Test
	@DisplayName("parentId 为 null 或 0 时规范化为 null")
	void normalizeParentId_rootIsNull() {
		assertThat(SysMenuListVoConverter.normalizeParentId(null)).isNull();
		assertThat(SysMenuListVoConverter.normalizeParentId(0L)).isNull();
		assertThat(SysMenuListVoConverter.normalizeParentId(10L)).isEqualTo(10L);
	}

	@Test
	@DisplayName("fromEntity 填充列表字段与 0/1 标志位")
	void fromEntity_mapsFields() {
		SysMenuEntity e = new SysMenuEntity();
		e.setId(100L);
		e.setParentId(0L);
		e.setName("sysUser");
		e.setTitle("用户管理");
		e.setPath("/user");
		e.setComponent("layout/index");
		e.setMenuType(0);
		e.setStatus(true);
		e.setShowLink(true);
		e.setMenuRank(5);
		e.setKeepAlive(false);
		e.setFixedTag(true);
		e.setRemark("r");
		e.setCreateTime(LocalDateTime.of(2026, 1, 1, 12, 0));
		e.setUpdateTime(LocalDateTime.of(2026, 1, 2, 12, 0));

		SysMenuListVO vo = SysMenuListVoConverter.fromEntity(e);
		assertThat(vo.getId()).isEqualTo(100L);
		assertThat(vo.getParentId()).isNull();
		assertThat(vo.getName()).isEqualTo("sysUser");
		assertThat(vo.getStatus()).isZero();
		assertThat(vo.getShowLink()).isEqualTo(1);
		assertThat(vo.getKeepAlive()).isZero();
		assertThat(vo.getFixedTag()).isEqualTo(1);
		assertThat(vo.getRemark()).isEqualTo("r");
	}

	@Test
	@DisplayName("toDetailVO 在列表字段基础上带出扩展元数据")
	void toDetailVO_mapsExtendedFields() {
		SysMenuEntity e = new SysMenuEntity();
		e.setId(1L);
		e.setParentId(0L);
		e.setName("m");
		e.setTitle("t");
		e.setPath("/p");
		e.setMenuType(0);
		e.setStatus(true);
		e.setShowLink(true);
		e.setMenuRank(1);
		e.setKeepAlive(false);
		e.setFixedTag(false);
		e.setRedirect("/r");
		e.setIcon("ep:user");
		e.setShowParent(true);
		e.setFrameSrc("https://example.com");
		e.setFrameLoading(true);
		e.setTransitionName("fade");
		e.setHiddenTag(false);
		e.setDynamicLevel(3);
		e.setActivePath("/ap");

		SysMenuDetailVO d = SysMenuListVoConverter.toDetailVO(e);
		assertThat(d.getRedirect()).isEqualTo("/r");
		assertThat(d.getIcon()).isEqualTo("ep:user");
		assertThat(d.getShowParent()).isEqualTo(1);
		assertThat(d.getFrameSrc()).isEqualTo("https://example.com");
		assertThat(d.getFrameLoading()).isEqualTo(1);
		assertThat(d.getTransitionName()).isEqualTo("fade");
		assertThat(d.getHiddenTag()).isZero();
		assertThat(d.getDynamicLevel()).isEqualTo(3);
		assertThat(d.getActivePath()).isEqualTo("/ap");
	}

}
