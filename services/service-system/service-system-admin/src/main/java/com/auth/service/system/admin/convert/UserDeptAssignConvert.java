package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.entity.UserDeptEntity;
import com.auth.service.system.admin.model.enums.UserOrgRelationStatusEnum;
import com.auth.service.system.admin.model.form.userdept.UserDeptAssignForm;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

/**
 * 用户部门关联表单与实体组装（含业务字段规则，复杂度否决 MapStruct）
 *
 * @author Bunny
 */
@UtilityClass
public class UserDeptAssignConvert {

	/**
	 * 新增：组装待插入实体
	 * @param form 关联表单
	 * @param isPrimary 是否主部门（由 Service 按规则解析）
	 * @return 待持久化实体
	 */
	public static UserDeptEntity toCreateEntity(UserDeptAssignForm form, boolean isPrimary) {
		UserDeptEntity entity = new UserDeptEntity();
		entity.setUserId(form.getUserId());
		entity.setDeptId(form.getDeptId());
		entity.setStatus(form.getStatus());
		entity.setPosition(form.getPosition());
		entity.setRemark(form.getRemark());
		entity.setGrantTime(LocalDateTime.now());
		entity.setIsPrimary(isPrimary);
		return entity;
	}

	/**
	 * 更新：组装需 patch 的字段
	 * @param relationId 关联主键
	 * @param form 关联表单
	 * @return 待更新实体
	 */
	public static UserDeptEntity toUpdateEntity(Long relationId, UserDeptAssignForm form) {
		UserDeptEntity entity = new UserDeptEntity();
		entity.setId(relationId);
		entity.setStatus(form.getStatus());
		entity.setPosition(form.getPosition());
		entity.setRemark(form.getRemark());
		if (!UserOrgRelationStatusEnum.canBePrimary(form.getStatus())) {
			entity.setIsPrimary(false);
		}
		else if (Boolean.FALSE.equals(form.getIsPrimary())) {
			entity.setIsPrimary(false);
		}
		return entity;
	}

}
