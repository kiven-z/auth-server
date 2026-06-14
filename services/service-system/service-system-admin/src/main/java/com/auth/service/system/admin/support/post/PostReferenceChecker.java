package com.auth.service.system.admin.support.post;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.PostReferenceMapper;
import com.auth.service.system.admin.mapper.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 岗位写操作前的存在性与引用校验
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class PostReferenceChecker {

	private final SysPostMapper sysPostMapper;

	private final PostReferenceMapper postReferenceMapper;

	/**
	 * 加载未逻辑删除的岗位，不存在则抛业务异常
	 * @param postId 岗位 ID
	 * @return 岗位实体
	 */
	public SysPostEntity getExistingActive(Long postId) {
		SysPostEntity existing = sysPostMapper.selectById(postId);
		if (existing == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, postId);
		}
		return existing;
	}

	/**
	 * 校验部门可挂载岗位（存在、启用、未删除）
	 * @param deptId 部门 ID
	 */
	public void requireAssignableDept(Long deptId) {
		if (sysPostMapper.countAssignableDept(deptId) == 0) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE, deptId);
		}
	}

	/**
	 * 同一部门内岗位编码唯一
	 * @param deptId 部门 ID
	 * @param postCode 岗位编码
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

	/**
	 * 逻辑删除前：无有效 user_post 关联，且 grant_table 无 POST 主体引用
	 * @param postId 岗位 ID
	 */
	public void requireDeletable(Long postId) {
		if (postReferenceMapper.countUserPostByPostId(postId) > 0
				|| postReferenceMapper.countGrantPostSubjectByPostId(postId) > 0) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_IN_USE, postId);
		}
	}

}
