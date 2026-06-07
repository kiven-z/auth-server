package com.auth.service.system.common.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.model.response.BaseResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.common.mapper.SysUserQueryMapper;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 审计用户展示名
 *
 * @author Bunny
 */
@Service
public class AuditUserDisplayServiceImpl implements AuditUserDisplayService {

	private final SysUserQueryMapper sysUserQueryMapper;

	public AuditUserDisplayServiceImpl(SysUserQueryMapper sysUserQueryMapper) {
		this.sysUserQueryMapper = sysUserQueryMapper;
	}

	/**
	 * 从行列表中收集创建人、更新人用户 ID
	 * @param records 行列表
	 * @return 用户主键列表
	 */
	private static List<Long> collectAuditUserIds(List<? extends BaseResponse> records) {
		return records.stream()
			.flatMap(vo -> Stream.of(vo.getCreateUser(), vo.getUpdateUser()))
			.filter(Objects::nonNull)
			.filter(userId -> userId > 0)
			.distinct()
			.toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Long, String> mapUsernamesByIds(Collection<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return Collections.emptyMap();
		}
		List<Long> distinctIds = userIds.stream()
			.filter(Objects::nonNull)
			.filter(userId -> userId > 0)
			.distinct()
			.toList();
		if (CollUtil.isEmpty(distinctIds)) {
			return Collections.emptyMap();
		}
		return sysUserQueryMapper.selectBatchIds(distinctIds)
			.stream()
			.collect(Collectors.toMap(UserEntity::getId, UserEntity::getUsername));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends BaseResponse> void enrichAuditUsernames(IPage<T> page) {
		if (page == null) {
			return;
		}
		enrichAuditUsernames(page.getRecords());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enrichAuditUsernames(List<? extends BaseResponse> records) {
		if (CollUtil.isEmpty(records)) {
			return;
		}
		Map<Long, String> usernameMap = mapUsernamesByIds(collectAuditUserIds(records));
		for (BaseResponse vo : records) {
			vo.setCreateUsername(usernameMap.get(vo.getCreateUser()));
			vo.setUpdateUsername(usernameMap.get(vo.getUpdateUser()));
		}
	}

}
