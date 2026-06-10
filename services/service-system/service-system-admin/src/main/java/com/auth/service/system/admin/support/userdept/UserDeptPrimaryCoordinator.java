package com.auth.service.system.admin.support.userdept;

import java.util.List;

import org.springframework.stereotype.Component;

import com.auth.service.system.admin.mapper.UserDeptMapper;
import com.auth.service.system.admin.model.entity.UserDeptEntity;
import com.auth.service.system.admin.model.enums.UserOrgRelationStatusEnum;

import lombok.RequiredArgsConstructor;

/**
 * 用户主部门一致性维护
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class UserDeptPrimaryCoordinator {

	private final UserDeptMapper userDeptMapper;

	/**
	 * 将指定关联设为主部门（调用方需保证 status=正常）
	 * 
	 * @param userId     用户 ID
	 * @param relationId 关联主键
	 */
	public void promotePrimary(Long userId, Long relationId) {
		userDeptMapper.demotePrimaryByUserId(userId);
		UserDeptEntity target = new UserDeptEntity();
		target.setId(relationId);
		target.setIsPrimary(true);
		userDeptMapper.updateById(target);
	}

	/**
	 * 变更后确保：存在有效关联时恰好有一个主部门（优先提升最早的正常关联）
	 * 
	 * @param userId 用户 ID
	 */
	public void ensurePrimaryInvariant(Long userId) {
		List<UserDeptEntity> activeRows = userDeptMapper.selectActiveByUserId(userId);
		if (activeRows.isEmpty()) {
			return;
		}

		long primaryCount = activeRows.stream().filter(row -> Boolean.TRUE.equals(row.getIsPrimary())).count();
		if (primaryCount == 1) {
			return;
		}

		userDeptMapper.demotePrimaryByUserId(userId);
		UserDeptEntity candidate = activeRows.stream()
				.filter(row -> UserOrgRelationStatusEnum.canBePrimary(row.getStatus()))
				.findFirst()
				.orElse(null);
		if (candidate == null) {
			return;
		}

		UserDeptEntity target = new UserDeptEntity();
		target.setId(candidate.getId());
		target.setIsPrimary(true);
		userDeptMapper.updateById(target);
	}

	/**
	 * 非主部门资格的状态需清除主部门标记
	 * 
	 * @param entity 待写入实体
	 */
	public void normalizePrimaryFlag(UserDeptEntity entity) {
		// FIXMe 删除这个函数
		if (!UserOrgRelationStatusEnum.canBePrimary(entity.getStatus())) {
			entity.setIsPrimary(false);
		}
	}

}
