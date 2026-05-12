package com.auth.service.system.common.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.model.response.BaseResponse;
import com.auth.module.platform.persistence.UserEntity;
import com.auth.service.system.admin.mapper.SysUserMapper;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 审计用户展示名：基于用户表批量解析用户名。
 *
 * @author Bunny
 */
@Service
public class AuditUserDisplayServiceImpl implements AuditUserDisplayService {

	private final SysUserMapper sysUserMapper;

	public AuditUserDisplayServiceImpl(SysUserMapper sysUserMapper) {
		this.sysUserMapper = sysUserMapper;
	}

	/**
	 * 从行列表中收集创建人、更新人用户 ID（去重、非空）。
	 * @param records 行列表
	 * @return 用户主键列表
	 */
	private static List<Long> collectAuditUserIds(List<? extends BaseResponse> records) {
		return records.stream()
			.flatMap(vo -> Stream.of(vo.getCreateUser(), vo.getUpdateUser()).filter(Objects::nonNull))
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
		List<Long> distinctIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(distinctIds)) {
			return Collections.emptyMap();
		}
		return sysUserMapper.selectBatchIds(distinctIds)
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
