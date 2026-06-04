package com.auth.service.system.admin.support.post;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 岗位写操作前的存在性与业务键校验
 *
 * @author bunny
 */
@RequiredArgsConstructor
@Component
public class PostReferenceChecker {

	private final SysPostMapper sysPostMapper;

	/**
	 * 加载未逻辑删除的岗位，不存在则抛业务异常
	 */
	public SysPostEntity getExistingActive(Long postId) {
		SysPostEntity existing = sysPostMapper.selectById(postId);
		if (existing == null) {
			throw new SystemBusinessException(SystemAdminResultCode.POST_NOT_FOUND, postId);
		}
		return existing;
	}

	/**
	 * 校验部门可挂载岗位（存在、启用、未删除）
	 */
	public void requireAssignableDept(Long deptId) {
		if (sysPostMapper.countAssignableDept(deptId) == 0) {
			throw new SystemBusinessException(SystemAdminResultCode.POST_DEPT_NOT_FOUND, deptId);
		}
	}

	/**
	 * 同一部门内岗位编码唯一
	 * @param excludePostId 新增传 null，更新传当前岗位 id
	 */
	public void requirePostCodeUniqueInDept(Long deptId, String postCode, Long excludePostId) {
		var wrapper = Wrappers.<SysPostEntity>lambdaQuery()
			.eq(SysPostEntity::getDeptId, deptId)
			.eq(SysPostEntity::getPostCode, postCode);

		if (excludePostId != null) {
			wrapper.ne(SysPostEntity::getId, excludePostId);
		}

		if (sysPostMapper.selectCount(wrapper) > 0) {
			throw new SystemBusinessException(SystemAdminResultCode.POST_CODE_DUPLICATE_IN_DEPT, deptId, postCode);
		}
	}

}