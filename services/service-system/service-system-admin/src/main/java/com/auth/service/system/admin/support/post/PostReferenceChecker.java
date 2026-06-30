package com.auth.service.system.admin.support.post;

import com.auth.service.system.admin.mapper.post.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * 跨服务共用的岗位存在性校验。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class PostReferenceChecker {

	private final SysPostMapper sysPostMapper;

	/**
	 * 加载岗位主表行，不存在则抛业务异常
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

}
