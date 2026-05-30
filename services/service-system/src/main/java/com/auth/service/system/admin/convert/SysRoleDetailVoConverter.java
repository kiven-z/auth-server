package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.enums.RoleTypeEnum;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import lombok.experimental.UtilityClass;

/**
 * 角色详情 VO 组装
 *
 * @author Bunny
 */
@UtilityClass
public class SysRoleDetailVoConverter {

	/**
	 * 实体 → 详情 VO（含角色类型描述）
	 * @param entity 角色实体
	 * @return 详情 VO
	 */
	public static SysRoleDetailVO toDetailVo(SysRoleEntity entity) {
		SysRoleDetailVO vo = SysRoleConverter.INSTANCE.toDetailVo(entity);

		String desc = RoleTypeEnum.getDesc(entity.getRoleType());
		vo.setRoleTypeDesc(desc);
		return vo;
	}

}
