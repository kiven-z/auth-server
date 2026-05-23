package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SysRoleDetailVoConverter} 映射校验
 */
class SysRoleDetailVoConverterTest {

	@Test
	@DisplayName("详情 VO 包含 MapStruct 字段与枚举描述")
	void toDetailVo_setsRoleTypeDesc() {
		SysRoleEntity entity = new SysRoleEntity();
		entity.setId(1L);
		entity.setRoleName("管理员");
		entity.setRoleCode("admin");
		entity.setRoleType(1);

		SysRoleDetailVO vo = SysRoleDetailVoConverter.toDetailVo(entity);

		assertThat(vo.getRoleName()).isEqualTo("管理员");
		assertThat(vo.getRoleTypeDesc()).isEqualTo("系统角色");
	}

}
